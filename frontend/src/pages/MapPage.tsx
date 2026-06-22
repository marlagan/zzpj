import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import NoticeMap, { getSearchRadiusForNotice } from "../components/NoticeMap";
import PopUp from "../components/PopUp";
import { getLocationsNear, searchByArea } from "../api/mapApi";
import { createNotice, getNoticeById, getNoticesByType } from "../api/noticeApi";
import { getStoredUser } from "../api/apiClient";
import type { User } from "../types/auth";
import type { Notice } from "../types/notice";
import type { GeoLocation, MapNoticeMarker, MapPoint } from "../types/map";
import { calculateSearchRadiusKm, distanceKm, parseGeoPoint } from "../utils/geo";
import { buildSightingNotes, isVisibleOnAnimalMap } from "../utils/noticeMap";

const DEFAULT_CENTER: [number, number] = [52.2297, 21.0122];
const BROWSE_RADIUS_KM = calculateSearchRadiusKm("dog", 3);

const styles: Record<string, React.CSSProperties> = {
    page: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        minHeight: 0,
        overflow: "hidden",
        width: "100%",
        alignSelf: "stretch",
        textAlign: "left",
        background: "#ffffff",
        color: "#000000",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    toolbar: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: "12px",
        padding: "12px 15px",
        borderBottom: "3px solid #222",
        background: "#f4f3ec",
        flexShrink: 0,
        width: "100%",
        boxSizing: "border-box",
        textAlign: "center",
    },
    toolbarActions: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "10px",
        flexWrap: "wrap",
        width: "100%",
    },
    button: {
        background: "#222",
        color: "white",
        border: "none",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        padding: "10px 16px",
        textDecoration: "none",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        whiteSpace: "nowrap",
        boxSizing: "border-box",
    },
    secondaryButton: {
        background: "#fff",
        color: "#222",
        border: "3px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        padding: "7px 14px",
        whiteSpace: "nowrap",
        boxSizing: "border-box",
    },
    activeButton: {
        background: "#aa3bff",
        color: "#fff",
        border: "3px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        padding: "7px 14px",
        whiteSpace: "nowrap",
        boxSizing: "border-box",
    },
    mapArea: {
        width: "100%",
        height: "420px",
        flexShrink: 0,
        padding: "12px",
        boxSizing: "border-box",
    },
    panel: {
        flex: 1,
        minHeight: 0,
        overflowY: "auto",
        padding: "15px",
        borderTop: "3px solid #222",
        background: "#fff",
        width: "100%",
        boxSizing: "border-box",
        textAlign: "left",
    },
    title: {
        fontSize: "22px",
        fontWeight: "bold",
        letterSpacing: "2px",
        marginBottom: "12px",
    },
    hint: {
        fontFamily: '"Space Mono", monospace',
        fontSize: "13px",
        color: "#555",
        lineHeight: 1.5,
        marginBottom: "12px",
    },
    detailCard: {
        border: "3px solid #222",
        padding: "12px",
        background: "#f4f3ec",
        fontFamily: '"Space Mono", monospace',
        fontSize: "13px",
        lineHeight: 1.6,
        marginBottom: "12px",
    },
    badge: {
        display: "inline-block",
        border: "2px solid #222",
        padding: "4px 8px",
        fontWeight: "bold",
        marginBottom: "10px",
        fontSize: "12px",
    },
    photo: {
        width: "100%",
        maxHeight: "160px",
        objectFit: "cover",
        border: "3px solid #222",
        marginBottom: "10px",
        imageRendering: "pixelated",
    },
    input: {
        padding: "10px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        width: "100%",
        boxSizing: "border-box",
        marginBottom: "10px",
    },
    errorText: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
        fontSize: "13px",
        marginBottom: "10px",
    },
};

function toLocalDateTime(value: string): string {
    return value.length === 16 ? `${value}:00` : value;
}

function buildMarkersFromNotices(
    notices: Notice[],
    geoLocations: GeoLocation[],
): { markers: MapNoticeMarker[]; byId: Record<string, Notice> } {
    const activeIds = new Set(notices.map((n) => n.id));
    const activeGeoLocations = geoLocations.filter((g) => activeIds.has(g.noticeId));

    const markers: MapNoticeMarker[] = notices.map((notice) => {
        const geo = activeGeoLocations.find((g) => g.noticeId === notice.id);
        const coords = geo ? parseGeoPoint(geo.location) : null;
        return {
            noticeId: notice.id,
            latitude: coords?.lat ?? notice.latitude,
            longitude: coords?.lng ?? notice.longitude,
            noticeType: notice.type,
            species: notice.species,
        };
    });

    const byId: Record<string, Notice> = {};
    notices.forEach((n) => {
        byId[n.id] = n;
    });

    return { markers, byId };
}

export default function MapPage() {
    const [searchParams] = useSearchParams();
    const storedUser = getStoredUser<User>();

    const [center, setCenter] = useState<[number, number]>(DEFAULT_CENTER);
    const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
    const [recenterKey, setRecenterKey] = useState(0);
    const [markers, setMarkers] = useState<MapNoticeMarker[]>([]);
    const [noticesById, setNoticesById] = useState<Record<string, Notice>>({});
    const [selectedNoticeId, setSelectedNoticeId] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [pickingSightingLocation, setPickingSightingLocation] = useState(false);
    const [sightingLat, setSightingLat] = useState<number | null>(null);
    const [sightingLng, setSightingLng] = useState<number | null>(null);
    const [sightingNotes, setSightingNotes] = useState("");
    const [sightingDate, setSightingDate] = useState("");
    const [sightingError, setSightingError] = useState("");
    const [sightingSubmitting, setSightingSubmitting] = useState(false);
    const [showSightingSuccess, setShowSightingSuccess] = useState(false);

    const [isDrawingArea, setIsDrawingArea] = useState(false);
    const [areaPolygonPoints, setAreaPolygonPoints] = useState<MapPoint[]>([]);
    const [isAreaFilterActive, setIsAreaFilterActive] = useState(false);
    const [areaSearchError, setAreaSearchError] = useState("");
    const [areaSearching, setAreaSearching] = useState(false);

    const selectedNotice = selectedNoticeId ? noticesById[selectedNoticeId] : null;

    const searchRadiusMeters = useMemo(() => {
        if (!selectedNotice || selectedNotice.type !== "LOST") return null;
        return getSearchRadiusForNotice(selectedNotice);
    }, [selectedNotice]);

    const loadMapData = useCallback(async (lat: number, lng: number) => {
        setLoading(true);
        setLoadError("");

        let geoLocations: GeoLocation[] = [];
        try {
            geoLocations = await getLocationsNear(lat, lng, BROWSE_RADIUS_KM);
        } catch {
            geoLocations = [];
        }

        let loadedNotices: Notice[] = [];
        try {
            const [lost, found] = await Promise.all([
                getNoticesByType("LOST", "ACTIVE"),
                getNoticesByType("FOUND", "ACTIVE"),
            ]);
            loadedNotices = [...lost, ...found];
        } catch {
            setLoadError("Failed to load animals on the map");
            setLoading(false);
            return;
        }

        if (geoLocations.length > 0) {
            const existingIds = new Set(loadedNotices.map((n) => n.id));
            const missingIds = [
                ...new Set(
                    geoLocations
                        .map((g) => g.noticeId)
                        .filter((id) => !existingIds.has(id)),
                ),
            ];
            const extras = await Promise.all(
                missingIds.map((id) => getNoticeById(id).catch(() => null)),
            );
            loadedNotices.push(...extras.filter((n): n is Notice => n != null && isVisibleOnAnimalMap(n)));
        }

        loadedNotices = loadedNotices.filter(isVisibleOnAnimalMap);

        let visibleNotices = loadedNotices.filter(
            (n) => distanceKm(lat, lng, n.latitude, n.longitude) <= BROWSE_RADIUS_KM,
        );
        if (visibleNotices.length === 0) {
            visibleNotices = loadedNotices;
        }

        visibleNotices = visibleNotices.filter(isVisibleOnAnimalMap);

        const { markers: nextMarkers, byId } = buildMarkersFromNotices(visibleNotices, geoLocations);

        setMarkers(nextMarkers);
        setNoticesById(byId);
        setSelectedNoticeId((prev) => (prev && byId[prev] ? prev : null));
        setIsAreaFilterActive(false);
        setAreaPolygonPoints([]);
        setIsDrawingArea(false);
        setAreaSearchError("");
        setLoading(false);
    }, []);

    useEffect(() => {
        if (!navigator.geolocation) {
            loadMapData(DEFAULT_CENTER[0], DEFAULT_CENTER[1]);
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const lat = Number(pos.coords.latitude.toFixed(6));
                const lng = Number(pos.coords.longitude.toFixed(6));
                setCenter([lat, lng]);
                setUserLocation([lat, lng]);
                loadMapData(lat, lng);
            },
            () => {
                loadMapData(DEFAULT_CENTER[0], DEFAULT_CENTER[1]);
            },
        );
    }, [loadMapData]);

    useEffect(() => {
        if (!selectedNoticeId) return;
        const notice = noticesById[selectedNoticeId];
        if (!notice || !isVisibleOnAnimalMap(notice)) {
            setSelectedNoticeId(null);
            setPickingSightingLocation(false);
        }
    }, [selectedNoticeId, noticesById]);

    useEffect(() => {
        const action = searchParams.get("action");
        if (action === "FOUND") {
            setPickingSightingLocation(false);
        }
    }, [searchParams]);

    const clearAreaSelection = useCallback(() => {
        setIsAreaFilterActive(false);
        setIsDrawingArea(false);
        setAreaPolygonPoints([]);
        setAreaSearchError("");
        setSelectedNoticeId(null);
        loadMapData(center[0], center[1]);
    }, [center, loadMapData]);

    const startDrawingArea = () => {
        setIsDrawingArea(true);
        setIsAreaFilterActive(false);
        setAreaPolygonPoints([]);
        setAreaSearchError("");
        setSelectedNoticeId(null);
        setPickingSightingLocation(false);
        setSightingLat(null);
        setSightingLng(null);
        setSightingError("");
    };

    const cancelDrawingArea = () => {
        setIsDrawingArea(false);
        setAreaPolygonPoints([]);
        setAreaSearchError("");
        if (isAreaFilterActive) return;
        loadMapData(center[0], center[1]);
    };

    const handleSearchInArea = async () => {
        if (areaPolygonPoints.length < 3) {
            setAreaSearchError("Draw at least 3 points on the map");
            return;
        }

        setAreaSearching(true);
        setAreaSearchError("");

        try {
            const geoLocations = await searchByArea({
                polygonPoints: areaPolygonPoints,
            });

            const noticeIds = [...new Set(geoLocations.map((g) => g.noticeId))];
            if (noticeIds.length === 0) {
                setMarkers([]);
                setNoticesById({});
                setSelectedNoticeId(null);
                setIsAreaFilterActive(true);
                setIsDrawingArea(false);
                return;
            }

            const notices = await Promise.all(
                noticeIds.map((id) => getNoticeById(id).catch(() => null)),
            );
            const visibleNotices = notices.filter(
                (n): n is Notice => n != null && isVisibleOnAnimalMap(n),
            );

            const { markers: nextMarkers, byId } = buildMarkersFromNotices(
                visibleNotices,
                geoLocations,
            );

            setMarkers(nextMarkers);
            setNoticesById(byId);
            setSelectedNoticeId(null);
            setIsAreaFilterActive(true);
            setIsDrawingArea(false);
        } catch (err) {
            setAreaSearchError(
                err instanceof Error ? err.message : "Area search failed",
            );
        } finally {
            setAreaSearching(false);
        }
    };

    const handleMapClick = (lat: number, lng: number) => {
        if (isDrawingArea) {
            setAreaPolygonPoints((prev) => [
                ...prev,
                { lat: Number(lat.toFixed(6)), lon: Number(lng.toFixed(6)) },
            ]);
            setAreaSearchError("");
            return;
        }

        if (pickingSightingLocation) {
            setSightingLat(Number(lat.toFixed(6)));
            setSightingLng(Number(lng.toFixed(6)));
            setSightingError("");
            return;
        }

        if (isAreaFilterActive) {
            clearAreaSelection();
            return;
        }

        setSelectedNoticeId(null);
        setPickingSightingLocation(false);
        setSightingLat(null);
        setSightingLng(null);
        setSightingError("");
        if (userLocation) {
            setRecenterKey((k) => k + 1);
        }
    };

    const handleUseMyLocationForSighting = () => {
        if (!navigator.geolocation) {
            setSightingError("Geolocation is not supported in this browser");
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                setSightingLat(Number(pos.coords.latitude.toFixed(6)));
                setSightingLng(Number(pos.coords.longitude.toFixed(6)));
                setSightingError("");
            },
            () => setSightingError("Could not get your location"),
        );
    };

    const handleSightingSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSightingError("");

        if (!selectedNotice || selectedNotice.type !== "LOST") return;
        if (!storedUser?.id) {
            setSightingError("You must be logged in");
            return;
        }
        if (sightingLat == null || sightingLng == null) {
            setSightingError("Please pick where you saw the animal on the map");
            return;
        }
        if (!sightingDate) {
            setSightingError("Sighting date is required");
            return;
        }

        setSightingSubmitting(true);
        try {
            await createNotice({
                type: "FOUND",
                reportedByUserId: storedUser.id,
                species: selectedNotice.species,
                breed: selectedNotice.breed,
                colorDescription: selectedNotice.colorDescription,
                additionalNotes: buildSightingNotes(selectedNotice.species, sightingNotes),
                latitude: sightingLat,
                longitude: sightingLng,
                eventDate: toLocalDateTime(sightingDate),
            });
            setShowSightingSuccess(true);
            setPickingSightingLocation(false);
            setSightingNotes("");
            setSightingDate("");
            setSightingLat(null);
            setSightingLng(null);
            await loadMapData(center[0], center[1]);
        } catch (err) {
            setSightingError(err instanceof Error ? err.message : "Failed to report sighting");
        } finally {
            setSightingSubmitting(false);
        }
    };

    const description =
        selectedNotice?.aiGeneratedDescription ||
        selectedNotice?.colorDescription ||
        selectedNotice?.additionalNotes ||
        "No description available";

    return (
        <div style={styles.page}>
            <div style={styles.toolbar}>
                <h1 style={{ ...styles.title, marginBottom: 0 }}>ANIMAL MAP</h1>
                <div style={styles.toolbarActions}>
                    <Link to="/notices/create/LOST" style={styles.button}>
                        + REPORT MISSING
                    </Link>
                    <Link to="/notices/create/FOUND" style={styles.button}>
                        + REPORT FOUND
                    </Link>
                    <button
                        type="button"
                        style={isDrawingArea ? styles.activeButton : styles.secondaryButton}
                        onClick={() => (isDrawingArea ? cancelDrawingArea() : startDrawingArea())}
                    >
                        {isDrawingArea ? "CANCEL DRAW" : "DRAW AREA"}
                    </button>
                    {isDrawingArea && (
                        <>
                            <button
                                type="button"
                                style={styles.secondaryButton}
                                onClick={() =>
                                    setAreaPolygonPoints((prev) => prev.slice(0, -1))
                                }
                                disabled={areaPolygonPoints.length === 0}
                            >
                                UNDO POINT
                            </button>
                            <button
                                type="button"
                                style={styles.button}
                                onClick={handleSearchInArea}
                                disabled={areaSearching || areaPolygonPoints.length < 3}
                            >
                                {areaSearching ? "SEARCHING..." : "SEARCH IN AREA"}
                            </button>
                        </>
                    )}
                    {isAreaFilterActive && !isDrawingArea && (
                        <button
                            type="button"
                            style={styles.secondaryButton}
                            onClick={clearAreaSelection}
                        >
                            CLEAR AREA
                        </button>
                    )}
                    <button
                        type="button"
                        style={styles.secondaryButton}
                        onClick={() => loadMapData(center[0], center[1])}
                    >
                        REFRESH
                    </button>
                </div>
            </div>

            <div style={styles.mapArea}>
                <NoticeMap
                    center={center}
                    markers={markers}
                    noticesById={noticesById}
                    selectedNoticeId={selectedNoticeId}
                    searchRadiusMeters={searchRadiusMeters}
                    onMarkerSelect={(id) => {
                        if (!isDrawingArea) setSelectedNoticeId(id);
                    }}
                    onMapClick={handleMapClick}
                    pickingSightingLocation={pickingSightingLocation}
                    sightingMarker={
                        sightingLat != null && sightingLng != null
                            ? { lat: sightingLat, lng: sightingLng }
                            : null
                    }
                    userLocation={userLocation}
                    recenterKey={recenterKey}
                    drawingArea={isDrawingArea}
                    areaPolygon={
                        (isDrawingArea || isAreaFilterActive) && areaPolygonPoints.length > 0
                            ? areaPolygonPoints
                            : null
                    }
                    autoFitBounds={!isDrawingArea}
                />
            </div>

            <aside style={styles.panel}>
                    <p style={styles.hint}>
                        {isDrawingArea
                            ? `Click the map to add polygon corners (${areaPolygonPoints.length} points). Need at least 3, then SEARCH IN AREA.`
                            : isAreaFilterActive
                              ? "Showing animals inside the selected area. Click the map or CLEAR AREA to return to the full map."
                              : `Showing animals within ${BROWSE_RADIUS_KM.toFixed(0)} km. Click a marker to read the description.`}
                    </p>

                    {areaSearchError && <p style={styles.errorText}>{areaSearchError}</p>}

                    {loading && <p style={styles.hint}>Loading map data...</p>}
                    {!loading && loadError && <p style={styles.errorText}>{loadError}</p>}
                    {!loading && !loadError && markers.length === 0 && (
                        <p style={styles.hint}>No active animals in this area yet.</p>
                    )}

                    {!selectedNotice && !loading && !isDrawingArea && (
                        <p style={styles.hint}>
                            {isAreaFilterActive
                                ? "Click the map to clear the area filter."
                                : "Select a marker, or click the map to clear selection and return to your location."}
                        </p>
                    )}

                    {selectedNotice && (
                        <>
                            <div style={styles.detailCard}>
                                <div style={styles.badge}>
                                    {selectedNotice.type === "LOST" ? "MISSING" : "FOUND"}
                                </div>
                                {selectedNotice.photoUrl && (
                                    <img src={selectedNotice.photoUrl} alt="pet" style={styles.photo} />
                                )}
                                <p><strong>Species:</strong> {selectedNotice.species}</p>
                                {selectedNotice.breed && <p><strong>Breed:</strong> {selectedNotice.breed}</p>}
                                <p><strong>Status:</strong> {selectedNotice.status}</p>
                                <p><strong>Description:</strong> {description}</p>
                                {selectedNotice.type === "LOST" && searchRadiusMeters != null && (
                                    <p>
                                        <strong>Search radius:</strong> {(searchRadiusMeters / 1000).toFixed(1)} km
                                    </p>
                                )}
                            </div>

                            {selectedNotice.type === "LOST" && !pickingSightingLocation && (
                                <button
                                    type="button"
                                    style={{ ...styles.button, width: "100%" }}
                                    onClick={() => {
                                        setPickingSightingLocation(true);
                                        setSightingError("");
                                    }}
                                >
                                    I SAW THIS ANIMAL
                                </button>
                            )}

                            {selectedNotice.type === "LOST" && pickingSightingLocation && (
                                <form onSubmit={handleSightingSubmit}>
                                    <p style={styles.hint}>
                                        Click on the map where you saw the animal, or use your location.
                                    </p>
                                    <button
                                        type="button"
                                        style={{ ...styles.secondaryButton, width: "100%", marginBottom: "10px" }}
                                        onClick={handleUseMyLocationForSighting}
                                    >
                                        USE MY LOCATION
                                    </button>
                                    {sightingLat != null && sightingLng != null && (
                                        <p style={styles.hint}>
                                            Location: {sightingLat}, {sightingLng}
                                        </p>
                                    )}
                                    <input
                                        style={styles.input}
                                        type="datetime-local"
                                        required
                                        value={sightingDate}
                                        onChange={(e) => setSightingDate(e.target.value)}
                                    />
                                    <textarea
                                        style={{ ...styles.input, minHeight: "70px", resize: "vertical" }}
                                        placeholder="What did you see? (optional)"
                                        value={sightingNotes}
                                        onChange={(e) => setSightingNotes(e.target.value)}
                                    />
                                    {sightingError && <p style={styles.errorText}>{sightingError}</p>}
                                    <button
                                        type="submit"
                                        style={{ ...styles.button, width: "100%", marginBottom: "8px" }}
                                        disabled={sightingSubmitting}
                                    >
                                        {sightingSubmitting ? "SUBMITTING..." : "SUBMIT SIGHTING"}
                                    </button>
                                    <button
                                        type="button"
                                        style={{ ...styles.secondaryButton, width: "100%" }}
                                        onClick={() => {
                                            setPickingSightingLocation(false);
                                            setSightingLat(null);
                                            setSightingLng(null);
                                            setSightingError("");
                                        }}
                                    >
                                        CANCEL
                                    </button>
                                </form>
                            )}
                        </>
                    )}
            </aside>

            <PopUp
                show={showSightingSuccess}
                onClose={() => setShowSightingSuccess(false)}
                title="SUCCESS"
                message="YOUR SIGHTING HAS BEEN REPORTED!"
            />
        </div>
    );
}

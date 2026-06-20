import React, { useCallback, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import PopUp from "../components/PopUp";
import LocationMapPicker from "../components/LocationMapPicker";
import { createNotice } from "../api/noticeApi";
import { getStoredUser } from "../api/apiClient";
import type { User } from "../types/auth";
import type { NoticeType } from "../types/notice";

const styles: Record<string, React.CSSProperties> = {
    page: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        background: "#ffffff",
        color: "#000000",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    main: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "flex-start",
        padding: "40px 15px 80px",
        gap: "20px",
    },
    title: {
        fontSize: "clamp(28px, 8vw, 42px)",
        fontWeight: "bold",
        letterSpacing: "3px",
        textAlign: "center",
    },
    card: {
        background: "white",
        border: "4px solid #222",
        padding: "clamp(15px, 5vw, 30px)",
        width: "100%",
        maxWidth: "550px",
        boxShadow: "6px 6px 0px #e5e4e7",
        boxSizing: "border-box",
    },
    inputGroup: {
        width: "100%",
        display: "flex",
        flexDirection: "column",
        gap: "8px",
        marginBottom: "15px",
    },
    label: {
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
        fontWeight: "bold",
    },
    input: {
        padding: "12px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        fontSize: "16px",
        outline: "none",
        width: "100%",
        boxSizing: "border-box",
    },
    button: {
        background: "#222",
        color: "white",
        border: "none",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "18px",
        padding: "12px",
    },
    secondaryButton: {
        background: "#f4f3ec",
        color: "#222",
        border: "3px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        padding: "10px",
        width: "100%",
    },
    errorText: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
    },
    hintText: {
        fontFamily: '"Space Mono", monospace',
        fontSize: "13px",
        color: "#555",
    },
    toggleGroup: {
        display: "flex",
        gap: "10px",
    },
    toggleOption: {
        flex: 1,
        padding: "12px",
        border: "3px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        fontWeight: "bold",
        background: "#f4f3ec",
        textAlign: "center",
    },
    toggleActive: {
        background: "#222",
        color: "white",
    },
};

function toLocalDateTime(value: string): string {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
}

function isValidNoticeType(type: string | undefined): type is NoticeType {
    return type === "LOST" || type === "FOUND";
}

type PetSpecies = "cat" | "dog";
type LocationMode = "gps" | "map";

export default function CreateNoticePage() {
    const { type: typeParam } = useParams<{ type: string }>();
    const navigate = useNavigate();
    const storedUser = getStoredUser<User>();

    const noticeType = isValidNoticeType(typeParam?.toUpperCase())
        ? (typeParam!.toUpperCase() as NoticeType)
        : null;

    const [species, setSpecies] = useState<PetSpecies | "">("");
    const [breed, setBreed] = useState("");
    const [colorDescription, setColorDescription] = useState("");
    const [additionalNotes, setAdditionalNotes] = useState("");
    const [locationMode, setLocationMode] = useState<LocationMode>("gps");
    const [latitude, setLatitude] = useState<number | null>(null);
    const [longitude, setLongitude] = useState<number | null>(null);
    const [eventDate, setEventDate] = useState("");
    const [error, setError] = useState("");
    const [locationError, setLocationError] = useState("");
    const [showSuccess, setShowSuccess] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const handleUseLocation = () => {
        setLocationError("");
        if (!navigator.geolocation) {
            setLocationError("Geolocation is not supported in this browser");
            return;
        }
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                setLatitude(Number(pos.coords.latitude.toFixed(6)));
                setLongitude(Number(pos.coords.longitude.toFixed(6)));
            },
            () => setLocationError("Could not get your location"),
        );
    };

    const handleMapLocationSelect = useCallback((lat: number, lng: number) => {
        setLatitude(Number(lat.toFixed(6)));
        setLongitude(Number(lng.toFixed(6)));
        setLocationError("");
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");

        if (!noticeType) {
            setError("Invalid notice type");
            return;
        }
        if (!storedUser?.id) {
            setError("You must be logged in");
            return;
        }
        if (latitude == null || longitude == null) {
            setError("Please select a location");
            return;
        }
        if (!species) {
            setError("Please select cat or dog");
            return;
        }
        if (!eventDate) {
            setError("Event date is required");
            return;
        }

        setSubmitting(true);
        try {
            await createNotice({
                type: noticeType,
                reportedByUserId: storedUser.id,
                species: species,
                breed: breed.trim() || undefined,
                colorDescription: colorDescription.trim() || undefined,
                additionalNotes: additionalNotes.trim() || undefined,
                latitude,
                longitude,
                eventDate: toLocalDateTime(eventDate),
            });
            setShowSuccess(true);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to create notice");
        } finally {
            setSubmitting(false);
        }
    };

    if (!noticeType) {
        return (
            <div style={styles.page}>
                <main style={styles.main}>
                    <h1 style={styles.title}>INVALID NOTICE TYPE</h1>
                    <p style={{ fontFamily: '"Space Mono", monospace' }}>Use /notices/create/LOST or /notices/create/FOUND</p>
                </main>
            </div>
        );
    }

    const title = noticeType === "LOST" ? "REPORT MISSING PET" : "REPORT FOUND PET";

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <Link to="/map" style={{ fontFamily: '"Pixelify Sans", sans-serif', fontWeight: "bold", color: "#222" }}>
                    ← BACK TO MAP
                </Link>
                <h1 style={styles.title}>{title}</h1>

                <div style={styles.card}>
                    <form onSubmit={handleSubmit}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>SPECIES *</label>
                            <div style={styles.toggleGroup}>
                                <button
                                    type="button"
                                    style={{ ...styles.toggleOption, ...(species === "cat" ? styles.toggleActive : {}) }}
                                    onClick={() => setSpecies("cat")}
                                >
                                    CAT
                                </button>
                                <button
                                    type="button"
                                    style={{ ...styles.toggleOption, ...(species === "dog" ? styles.toggleActive : {}) }}
                                    onClick={() => setSpecies("dog")}
                                >
                                    DOG
                                </button>
                            </div>
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>BREED</label>
                            <input style={styles.input} placeholder="optional" value={breed} onChange={(e) => setBreed(e.target.value)} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>COLOR / MARKINGS</label>
                            <input style={styles.input} placeholder="e.g. black and white" value={colorDescription} onChange={(e) => setColorDescription(e.target.value)} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>ADDITIONAL NOTES</label>
                            <textarea
                                style={{ ...styles.input, minHeight: "80px", resize: "vertical" }}
                                placeholder="anything else that helps identify the pet"
                                value={additionalNotes}
                                onChange={(e) => setAdditionalNotes(e.target.value)}
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>EVENT DATE *</label>
                            <input style={styles.input} type="datetime-local" required value={eventDate} onChange={(e) => setEventDate(e.target.value)} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>LOCATION *</label>
                            <div style={{ ...styles.toggleGroup, marginBottom: "10px" }}>
                                <button
                                    type="button"
                                    style={{ ...styles.toggleOption, ...(locationMode === "gps" ? styles.toggleActive : {}) }}
                                    onClick={() => setLocationMode("gps")}
                                >
                                    MY LOCATION
                                </button>
                                <button
                                    type="button"
                                    style={{ ...styles.toggleOption, ...(locationMode === "map" ? styles.toggleActive : {}) }}
                                    onClick={() => setLocationMode("map")}
                                >
                                    MAP
                                </button>
                            </div>

                            {locationMode === "gps" && (
                                <button type="button" style={styles.secondaryButton} onClick={handleUseLocation}>
                                    Use my location
                                </button>
                            )}

                            {locationMode === "map" && (
                                <>
                                    <p style={styles.hintText}>Click on the map to set the location</p>
                                    <LocationMapPicker
                                        latitude={latitude}
                                        longitude={longitude}
                                        onLocationSelect={handleMapLocationSelect}
                                    />
                                </>
                            )}

                            {locationError && <p style={styles.errorText}>{locationError}</p>}

                            {latitude != null && longitude != null && (
                                <p style={styles.hintText}>
                                    Selected: {latitude}, {longitude}
                                </p>
                            )}
                        </div>

                        {error && <p style={styles.errorText}>{error}</p>}

                        <button type="submit" style={{ ...styles.button, width: "100%" }} disabled={submitting}>
                            {submitting ? "SUBMITTING..." : "SUBMIT NOTICE"}
                        </button>
                    </form>
                </div>
            </main>

            <PopUp
                show={showSuccess}
                onClose={() => {
                    setShowSuccess(false);
                    navigate("/map");
                }}
                title="SUCCESS"
                message="YOUR NOTICE HAS BEEN CREATED!"
            />
        </div>
    );
}

import { useEffect, useRef, useState } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { Notice } from "../types/notice";
import type { MapNoticeMarker } from "../types/map";
import { calculateSearchRadiusMeters } from "../utils/geo";
import catMarkerImage from "../assets/cat8.jpg";
import dogMarkerImage from "../assets/dog.jpg";

const OSM_TILE_URL = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
const ALLOWED_SUBDOMAINS = ["a", "b", "c"] as const;
const DEFAULT_ZOOM = 13;
const BASE_MARKER_SIZE = 52;
const MIN_MARKER_SIZE = 26;
const MAX_MARKER_SIZE = 64;

type NoticeMapProps = {
    center: [number, number];
    markers: MapNoticeMarker[];
    noticesById: Record<string, Notice>;
    selectedNoticeId: string | null;
    searchRadiusMeters: number | null;
    onMarkerSelect: (noticeId: string) => void;
    onMapClick?: (lat: number, lng: number) => void;
    pickingSightingLocation?: boolean;
    sightingMarker?: { lat: number; lng: number } | null;
    userLocation?: [number, number] | null;
    recenterKey?: number;
};

function markerSizeForZoom(zoom: number): number {
    const size = Math.round(BASE_MARKER_SIZE * Math.pow(1.12, zoom - DEFAULT_ZOOM));
    return Math.min(MAX_MARKER_SIZE, Math.max(MIN_MARKER_SIZE, size));
}

function defaultPetImage(species?: string): string {
    const normalized = species?.trim().toLowerCase() ?? "";
    return normalized === "dog" ? dogMarkerImage : catMarkerImage;
}

function createPetIcon(
    species: string | undefined,
    noticeType: string,
    size: number,
): L.DivIcon {
    const borderColor = noticeType === "LOST" ? "#c0392b" : "#27ae60";
    const border = Math.max(2, Math.round(size * 0.075));
    const imageSrc = defaultPetImage(species);
    const anchor = size / 2;

    return L.divIcon({
        className: "notice-map-marker",
        html: `<div style="width:${size}px;height:${size}px;border:${border}px solid ${borderColor};background:#fff;box-shadow:3px 3px 0 #222;overflow:hidden;cursor:pointer;box-sizing:border-box;">
            <img src="${imageSrc}" alt="pet" style="width:100%;height:100%;object-fit:cover;image-rendering:pixelated;display:block;" />
        </div>`,
        iconSize: [size, size],
        iconAnchor: [anchor, anchor],
    });
}

function createSightingIcon(size: number): L.DivIcon {
    const anchor = size / 2;
    return L.divIcon({
        className: "notice-map-sighting",
        html: `<div style="width:${size}px;height:${size}px;border-radius:50%;background:#222;border:3px solid #fff;box-shadow:0 0 0 2px #222;box-sizing:border-box;"></div>`,
        iconSize: [size, size],
        iconAnchor: [anchor, anchor],
    });
}

export default function NoticeMap({
    center,
    markers,
    noticesById,
    selectedNoticeId,
    searchRadiusMeters,
    onMarkerSelect,
    onMapClick,
    pickingSightingLocation = false,
    sightingMarker = null,
    userLocation = null,
    recenterKey = 0,
}: NoticeMapProps) {
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<L.Map | null>(null);
    const markersLayerRef = useRef<L.LayerGroup | null>(null);
    const radiusCircleRef = useRef<L.Circle | null>(null);
    const sightingMarkerRef = useRef<L.Marker | null>(null);
    const onSelectRef = useRef(onMarkerSelect);
    const onMapClickRef = useRef(onMapClick);
    const [mapZoom, setMapZoom] = useState(DEFAULT_ZOOM);

    useEffect(() => {
        onSelectRef.current = onMarkerSelect;
    }, [onMarkerSelect]);

    useEffect(() => {
        onMapClickRef.current = onMapClick;
    }, [onMapClick]);

    useEffect(() => {
        if (!mapContainerRef.current || mapRef.current) return;

        const map = L.map(mapContainerRef.current, {
            maxZoom: 18,
            minZoom: 4,
        }).setView(center, DEFAULT_ZOOM);

        L.tileLayer(OSM_TILE_URL, {
            subdomains: [...ALLOWED_SUBDOMAINS],
            attribution: "&copy; OpenStreetMap contributors",
            maxZoom: 18,
        }).addTo(map);

        markersLayerRef.current = L.layerGroup().addTo(map);

        map.on("click", (e: L.LeafletMouseEvent) => {
            onMapClickRef.current?.(e.latlng.lat, e.latlng.lng);
        });

        const syncZoom = () => setMapZoom(map.getZoom());
        map.on("zoomend", syncZoom);
        syncZoom();

        mapRef.current = map;

        requestAnimationFrame(() => map.invalidateSize());

        return () => {
            map.remove();
            mapRef.current = null;
            markersLayerRef.current = null;
            radiusCircleRef.current = null;
            sightingMarkerRef.current = null;
        };
    }, []);

    useEffect(() => {
        const container = mapContainerRef.current;
        const map = mapRef.current;
        if (!container || !map) return;

        const resize = () => map.invalidateSize();
        const observer = new ResizeObserver(resize);
        observer.observe(container);
        resize();

        return () => observer.disconnect();
    }, []);

    useEffect(() => {
        mapRef.current?.setView(center, mapRef.current.getZoom());
    }, [center]);

    useEffect(() => {
        const map = mapRef.current;
        if (!map || !userLocation || recenterKey === 0) return;
        map.flyTo(userLocation, Math.max(map.getZoom(), DEFAULT_ZOOM), { duration: 0.6 });
    }, [recenterKey, userLocation]);

    useEffect(() => {
        const map = mapRef.current;
        if (!map || markers.length === 0) return;

        const bounds = L.latLngBounds(markers.map((m) => [m.latitude, m.longitude] as [number, number]));
        if (bounds.isValid()) {
            map.fitBounds(bounds.pad(0.2));
            setMapZoom(map.getZoom());
        }
    }, [markers]);

    useEffect(() => {
        const layer = markersLayerRef.current;
        if (!layer) return;

        const size = markerSizeForZoom(mapZoom);
        layer.clearLayers();

        markers.forEach((marker) => {
            const notice = noticesById[marker.noticeId];
            const species = marker.species || notice?.species;
            const icon = createPetIcon(species, marker.noticeType, size);
            const leafletMarker = L.marker([marker.latitude, marker.longitude], { icon });
            leafletMarker.on("click", (e) => {
                L.DomEvent.stopPropagation(e);
                onSelectRef.current(marker.noticeId);
            });
            layer.addLayer(leafletMarker);
        });
    }, [markers, noticesById, mapZoom]);

    useEffect(() => {
        const map = mapRef.current;
        if (!map) return;

        if (radiusCircleRef.current) {
            radiusCircleRef.current.remove();
            radiusCircleRef.current = null;
        }

        if (!selectedNoticeId || searchRadiusMeters == null) return;

        const selected = markers.find((m) => m.noticeId === selectedNoticeId);
        if (!selected) return;

        radiusCircleRef.current = L.circle([selected.latitude, selected.longitude], {
            radius: searchRadiusMeters,
            color: "#aa3bff",
            fillColor: "#aa3bff",
            fillOpacity: 0.12,
            weight: 2,
            dashArray: "6 4",
        }).addTo(map);
    }, [selectedNoticeId, searchRadiusMeters, markers]);

    useEffect(() => {
        const map = mapRef.current;
        if (!map) return;

        if (sightingMarkerRef.current) {
            sightingMarkerRef.current.remove();
            sightingMarkerRef.current = null;
        }

        if (!pickingSightingLocation || !sightingMarker) return;

        const size = Math.max(14, Math.round(markerSizeForZoom(mapZoom) * 0.4));
        sightingMarkerRef.current = L.marker([sightingMarker.lat, sightingMarker.lng], {
            icon: createSightingIcon(size),
        }).addTo(map);
    }, [pickingSightingLocation, sightingMarker, mapZoom]);

    return (
        <div
            ref={mapContainerRef}
            style={{
                width: "100%",
                height: "100%",
                border: "4px solid #222",
                boxSizing: "border-box",
                cursor: pickingSightingLocation ? "crosshair" : "grab",
            }}
        />
    );
}

export function getSearchRadiusForNotice(notice: Notice): number {
    const daysMissing = Math.max(
        1,
        Math.ceil((Date.now() - new Date(notice.eventDate).getTime()) / (1000 * 60 * 60 * 24)),
    );
    return calculateSearchRadiusMeters(notice.species, daysMissing);
}

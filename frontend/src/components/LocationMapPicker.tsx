import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

const OSM_TILE_URL = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
const ALLOWED_SUBDOMAINS = ["a", "b", "c"] as const;

const defaultIcon = L.icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon2x,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

L.Marker.prototype.options.icon = defaultIcon;

type LocationMapPickerProps = {
    latitude: number | null;
    longitude: number | null;
    onLocationSelect: (lat: number, lng: number) => void;
};

const DEFAULT_CENTER: [number, number] = [52.2297, 21.0122];
const DEFAULT_ZOOM = 13;

function isValidCoordinate(lat: number, lng: number): boolean {
    return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
}

export default function LocationMapPicker({
    latitude,
    longitude,
    onLocationSelect,
}: LocationMapPickerProps) {
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<L.Map | null>(null);
    const markerRef = useRef<L.Marker | null>(null);
    const onSelectRef = useRef(onLocationSelect);

    useEffect(() => {
        onSelectRef.current = onLocationSelect;
    }, [onLocationSelect]);

    useEffect(() => {
        if (!mapContainerRef.current || mapRef.current) return;

        const initialCenter: [number, number] =
            latitude != null && longitude != null
                ? [latitude, longitude]
                : DEFAULT_CENTER;

        const map = L.map(mapContainerRef.current, {
            maxZoom: 18,
            minZoom: 4,
        }).setView(initialCenter, DEFAULT_ZOOM);

        L.tileLayer(OSM_TILE_URL, {
            subdomains: [...ALLOWED_SUBDOMAINS],
            attribution: "&copy; OpenStreetMap contributors",
            maxZoom: 18,
        }).addTo(map);

        if (latitude != null && longitude != null && isValidCoordinate(latitude, longitude)) {
            markerRef.current = L.marker([latitude, longitude]).addTo(map);
        }

        map.on("click", (e: L.LeafletMouseEvent) => {
            const { lat, lng } = e.latlng;
            if (!isValidCoordinate(lat, lng)) return;

            if (markerRef.current) {
                markerRef.current.setLatLng(e.latlng);
            } else {
                markerRef.current = L.marker(e.latlng).addTo(map);
            }
            onSelectRef.current(lat, lng);
        });

        mapRef.current = map;

        return () => {
            map.remove();
            mapRef.current = null;
            markerRef.current = null;
        };
    }, []);

    useEffect(() => {
        if (!mapRef.current || latitude == null || longitude == null) return;
        if (!isValidCoordinate(latitude, longitude)) return;

        const pos: [number, number] = [latitude, longitude];
        if (markerRef.current) {
            markerRef.current.setLatLng(pos);
        } else {
            markerRef.current = L.marker(pos).addTo(mapRef.current);
        }
        mapRef.current.setView(pos, mapRef.current.getZoom());
    }, [latitude, longitude]);

    return (
        <div
            ref={mapContainerRef}
            style={{
                width: "100%",
                height: "260px",
                border: "3px solid #222",
                marginTop: "8px",
            }}
        />
    );
}

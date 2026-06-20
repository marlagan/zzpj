import type { GeoPoint } from "../types/map";

export function parseGeoPoint(point: GeoPoint | null | undefined): { lat: number; lng: number } | null {
    if (!point) return null;

    if ("coordinates" in point && Array.isArray(point.coordinates) && point.coordinates.length >= 2) {
        const [lng, lat] = point.coordinates;
        return { lat, lng };
    }

    if ("x" in point && "y" in point) {
        return { lat: point.y, lng: point.x };
    }

    return null;
}

/** Mirrors map-service LocationMatchingService.calculateSearchRadius (meters). */
export function calculateSearchRadiusMeters(species: string, daysMissing: number): number {
    const baseRadiusKm = species.toLowerCase() === "cat" ? 1.0 : 5.0;
    return baseRadiusKm * Math.max(daysMissing, 1) * 1000;
}

export function calculateSearchRadiusKm(species: string, daysMissing: number): number {
    return calculateSearchRadiusMeters(species, daysMissing) / 1000;
}

export function daysSince(dateStr: string): number {
    const diff = Date.now() - new Date(dateStr).getTime();
    return Math.max(1, Math.ceil(diff / (1000 * 60 * 60 * 24)));
}

export function distanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const toRad = (deg: number) => (deg * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

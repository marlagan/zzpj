import { apiFetch } from "./apiClient";
import type { GeoLocation } from "../types/map";

export const getLocationsNear = (lat: number, lon: number, radiusKm: number) =>
    apiFetch<GeoLocation[]>(
        `/api/maps/locations/near?lat=${lat}&lon=${lon}&radiusKm=${radiusKm}`,
    );

export const getMatchesForNotice = (noticeId: string, species: string, daysMissing: number) =>
    apiFetch<GeoLocation[]>(
        `/api/maps/locations/matches/${noticeId}?species=${encodeURIComponent(species)}&daysMissing=${daysMissing}`,
    );

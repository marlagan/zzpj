import type { Notice } from "../types/notice";

/** Prefix for FOUND notices created via map "I saw this animal" — hidden from map markers. */
export const SIGHTING_NOTE_PREFIX = "Possible sighting of missing";

export function buildSightingNotes(species: string, userNotes: string): string {
    return [ `${SIGHTING_NOTE_PREFIX} ${species}.`, userNotes.trim() ]
        .filter(Boolean)
        .join(" ");
}

export function isSightingFoundNotice(notice: Notice): boolean {
    return (
        notice.type === "FOUND" &&
        (notice.additionalNotes?.startsWith(SIGHTING_NOTE_PREFIX) ?? false)
    );
}

export function isVisibleOnAnimalMap(notice: Notice): boolean {
    return notice.status === "ACTIVE" && !isSightingFoundNotice(notice);
}

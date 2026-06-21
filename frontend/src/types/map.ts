export type GeoPoint =
    | { type: string; coordinates: [number, number] }
    | { x: number; y: number };

export type GeoLocation = {
    id: string;
    noticeId: string;
    noticeType: string;
    location: GeoPoint;
    accuracyRadiusMeters?: number | null;
    createdAt: string;
};

export type MapNoticeMarker = {
    noticeId: string;
    latitude: number;
    longitude: number;
    noticeType: string;
    species: string;
};

export type MapPoint = {
    lat: number;
    lon: number;
};

export type AreaSearchRequest = {
    polygonPoints: MapPoint[];
    species?: string | null;
    originType?: string | null;
};

export type NoticeType = "LOST" | "FOUND";

export type NoticeStatus = "ACTIVE" | "PENDING_MATCH" | "RESOLVED" | "CLOSED";

export type Notice = {
    id: string;
    type: NoticeType;
    status: NoticeStatus;
    reportedByUserId: string;
    species: string;
    breed?: string;
    colorDescription?: string;
    additionalNotes?: string;
    photoUrl?: string;
    aiGeneratedDescription?: string;
    latitude: number;
    longitude: number;
    eventDate: string;
    createdAt: string;
};

export type CreateNoticeRequest = {
    type: NoticeType;
    reportedByUserId: string;
    species: string;
    breed?: string;
    colorDescription?: string;
    additionalNotes?: string;
    photoUrl?: string;
    latitude: number;
    longitude: number;
    eventDate: string;
};

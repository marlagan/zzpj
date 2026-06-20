export type NoticeType = "LOST" | "FOUND";

export type NoticeStatus = "ACTIVE" | "PENDING_MATCH" | "RESOLVED" | "CLOSED";

export type Notice = {
    id: string;
    type: NoticeType;
    status: NoticeStatus;
    species: string;
    breed?: string;
    colorDescription?: string;
    createdAt: string;
};

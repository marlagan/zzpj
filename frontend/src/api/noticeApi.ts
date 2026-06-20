import { apiFetch } from "./apiClient";
import type { CreateNoticeRequest, Notice, NoticeStatus, NoticeType } from "../types/notice";

export const createNotice = (data: CreateNoticeRequest) =>
    apiFetch<Notice>("/api/notices", {
        method: "POST",
        body: JSON.stringify(data),
    });

export const getNoticeById = (id: string) =>
    apiFetch<Notice>(`/api/notices/${id}`);

export const getNoticesByType = (type: NoticeType, status: NoticeStatus = "ACTIVE") =>
    apiFetch<Notice[]>(`/api/notices?type=${type}&status=${status}`);

export const getNoticesByUser = (userId: string) =>
    apiFetch<Notice[]>(`/api/notices/user/${userId}`);

export const updateNoticeStatus = (id: string, status: NoticeStatus) =>
    apiFetch<Notice>(`/api/notices/${id}/status?status=${status}`, {
        method: "PATCH",
    });

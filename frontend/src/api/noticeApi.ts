import { apiFetch } from "./apiClient";
import type { Notice } from "../types/notice";

export const getNoticesByUser = (userId: string) =>
    apiFetch<Notice[]>(`/api/notices/user/${userId}`);

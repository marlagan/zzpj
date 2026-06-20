import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getNoticeById, updateNoticeStatus } from "../api/noticeApi";
import { getStoredUser } from "../api/apiClient";
import type { User } from "../types/auth";
import type { Notice, NoticeStatus } from "../types/notice";

const styles: Record<string, React.CSSProperties> = {
    page: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        background: "#ffffff",
        color: "#000000",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    main: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "flex-start",
        padding: "40px 15px",
        gap: "20px",
    },
    title: {
        fontSize: "clamp(28px, 8vw, 42px)",
        fontWeight: "bold",
        letterSpacing: "3px",
        textAlign: "center",
    },
    card: {
        background: "white",
        border: "4px solid #222",
        padding: "clamp(15px, 5vw, 30px)",
        width: "100%",
        maxWidth: "600px",
        boxShadow: "6px 6px 0px #e5e4e7",
        boxSizing: "border-box",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        lineHeight: 1.8,
    },
    badge: {
        display: "inline-block",
        border: "3px solid #222",
        padding: "6px 12px",
        background: "#f4f3ec",
        fontWeight: "bold",
        marginBottom: "15px",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    section: {
        marginTop: "15px",
        paddingTop: "15px",
        borderTop: "2px solid #ddd",
    },
    button: {
        background: "#222",
        color: "white",
        border: "none",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
        padding: "10px 16px",
        marginRight: "10px",
        marginTop: "10px",
    },
    link: {
        color: "#222",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontWeight: "bold",
    },
    errorText: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
    },
    photoPreview: {
        width: "100%",
        maxHeight: "280px",
        objectFit: "cover",
        border: "3px solid #222",
        marginTop: "8px",
        imageRendering: "pixelated",
    },
};

function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function NoticeDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const storedUser = getStoredUser<User>();

    const [notice, setNotice] = useState<Notice | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [updating, setUpdating] = useState(false);

    useEffect(() => {
        if (!id) return;
        setLoading(true);
        getNoticeById(id)
            .then(setNotice)
            .catch(() => setError("Failed to load notice"))
            .finally(() => setLoading(false));
    }, [id]);

    const isOwner = notice && storedUser?.id === notice.reportedByUserId;
    const canClose = notice && (notice.status === "ACTIVE" || notice.status === "PENDING_MATCH");

    const handleStatusChange = async (status: NoticeStatus) => {
        if (!notice) return;
        setUpdating(true);
        setError("");
        try {
            const updated = await updateNoticeStatus(notice.id, status);
            setNotice(updated);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to update status");
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return (
            <div style={styles.page}>
                <main style={styles.main}>
                    <p style={{ fontFamily: '"Space Mono", monospace' }}>Loading...</p>
                </main>
            </div>
        );
    }

    if (error && !notice) {
        return (
            <div style={styles.page}>
                <main style={styles.main}>
                    <p style={styles.errorText}>{error}</p>
                    <Link to="/notices" style={styles.link}>Back to notices</Link>
                </main>
            </div>
        );
    }

    if (!notice) return null;

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title}>NOTICE DETAILS</h1>

                <div style={styles.card}>
                    <div style={styles.badge}>
                        {notice.type === "LOST" ? "MISSING" : "FOUND"} · {notice.status}
                    </div>

                    <p><strong>Species:</strong> {notice.species}</p>
                    {notice.breed && <p><strong>Breed:</strong> {notice.breed}</p>}
                    {notice.colorDescription && <p><strong>Color:</strong> {notice.colorDescription}</p>}
                    {notice.additionalNotes && <p><strong>Notes:</strong> {notice.additionalNotes}</p>}
                    {notice.photoUrl && (
                        <div style={styles.section}>
                            <p><strong>Photo:</strong></p>
                            <img src={notice.photoUrl} alt="Pet" style={styles.photoPreview} />
                        </div>
                    )}

                    <div style={styles.section}>
                        <p><strong>Event date:</strong> {formatDate(notice.eventDate)}</p>
                        <p><strong>Created:</strong> {formatDate(notice.createdAt)}</p>
                        <p><strong>Location:</strong> {notice.latitude}, {notice.longitude}</p>
                    </div>

                    {notice.aiGeneratedDescription && (
                        <div style={styles.section}>
                            <p><strong>AI description:</strong></p>
                            <p>{notice.aiGeneratedDescription}</p>
                        </div>
                    )}

                    {isOwner && canClose && (
                        <div style={styles.section}>
                            <p style={{ fontFamily: '"Pixelify Sans", sans-serif', fontWeight: "bold" }}>MANAGE NOTICE</p>
                            <button
                                type="button"
                                style={styles.button}
                                disabled={updating}
                                onClick={() => handleStatusChange("RESOLVED")}
                            >
                                MARK RESOLVED
                            </button>
                            <button
                                type="button"
                                style={styles.button}
                                disabled={updating}
                                onClick={() => handleStatusChange("CLOSED")}
                            >
                                CLOSE NOTICE
                            </button>
                        </div>
                    )}

                    {error && <p style={styles.errorText}>{error}</p>}

                    <div style={{ marginTop: "20px", display: "flex", gap: "15px", flexWrap: "wrap" }}>
                        <Link to="/notices" style={styles.link}>← All notices</Link>
                        {isOwner && <Link to="/profile" style={styles.link}>My profile</Link>}
                        <button type="button" style={{ ...styles.button, marginTop: 0 }} onClick={() => navigate(-1)}>
                            BACK
                        </button>
                    </div>
                </div>
            </main>
        </div>
    );
}

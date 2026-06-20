import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getNoticesByType } from "../api/noticeApi";
import type { Notice, NoticeType } from "../types/notice";

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
        fontSize: "clamp(28px, 8vw, 48px)",
        fontWeight: "bold",
        letterSpacing: "3px",
        textAlign: "center",
    },
    tabs: {
        display: "flex",
        gap: "12px",
        flexWrap: "wrap",
        justifyContent: "center",
    },
    tab: {
        padding: "10px 20px",
        border: "3px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
        fontWeight: "bold",
        background: "#f4f3ec",
        textDecoration: "none",
        color: "#222",
    },
    tabActive: {
        background: "#222",
        color: "white",
    },
    list: {
        width: "100%",
        maxWidth: "600px",
        display: "flex",
        flexDirection: "column",
        gap: "12px",
    },
    item: {
        border: "3px solid #222",
        padding: "15px",
        background: "#f4f3ec",
        boxShadow: "4px 4px 0px #e5e4e7",
        textDecoration: "none",
        color: "#222",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        lineHeight: 1.6,
    },
    itemTitle: {
        fontFamily: '"Pixelify Sans", sans-serif',
        fontWeight: "bold",
        fontSize: "16px",
        marginBottom: "6px",
    },
    actions: {
        display: "flex",
        gap: "12px",
        flexWrap: "wrap",
        justifyContent: "center",
        marginTop: "10px",
    },
    createBtn: {
        padding: "12px 18px",
        background: "#222",
        color: "white",
        border: "none",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
        textDecoration: "none",
    },
    empty: {
        fontFamily: '"Space Mono", monospace',
        color: "#555",
        textAlign: "center",
    },
    error: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
    },
};

function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

export default function NoticesListPage() {
    const [activeTab, setActiveTab] = useState<NoticeType>("LOST");
    const [notices, setNotices] = useState<Notice[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        setLoading(true);
        setError("");
        getNoticesByType(activeTab, "ACTIVE")
            .then(setNotices)
            .catch(() => setError("Failed to load notices"))
            .finally(() => setLoading(false));
    }, [activeTab]);

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title}>ACTIVE NOTICES</h1>

                <div style={styles.tabs}>
                    <button
                        type="button"
                        style={{ ...styles.tab, ...(activeTab === "LOST" ? styles.tabActive : {}) }}
                        onClick={() => setActiveTab("LOST")}
                    >
                        MISSING (LOST)
                    </button>
                    <button
                        type="button"
                        style={{ ...styles.tab, ...(activeTab === "FOUND" ? styles.tabActive : {}) }}
                        onClick={() => setActiveTab("FOUND")}
                    >
                        FOUND
                    </button>
                </div>

                <div style={styles.actions}>
                    <Link to="/notices/create/LOST" style={styles.createBtn}>+ REPORT MISSING</Link>
                    <Link to="/notices/create/FOUND" style={styles.createBtn}>+ REPORT FOUND</Link>
                </div>

                {loading && <p style={styles.empty}>Loading...</p>}
                {error && <p style={styles.error}>{error}</p>}

                {!loading && !error && notices.length === 0 && (
                    <p style={styles.empty}>No active {activeTab} notices</p>
                )}

                <div style={styles.list}>
                    {!loading && !error && notices.map((notice) => (
                        <Link key={notice.id} to={`/notices/${notice.id}`} style={styles.item}>
                            <div style={styles.itemTitle}>
                                {notice.type === "LOST" ? "MISSING" : "FOUND"} · {notice.species}
                            </div>
                            {notice.breed && <div>Breed: {notice.breed}</div>}
                            {notice.colorDescription && <div>Color: {notice.colorDescription}</div>}
                            <div>Status: {notice.status}</div>
                            <div>Date: {formatDate(notice.createdAt)}</div>
                        </Link>
                    ))}
                </div>
            </main>
        </div>
    );
}

import React, {useState, useEffect} from 'react';
import PopUp from '../components/PopUp';
import {changePassword} from "../api/authApi.ts";
import {getNoticesByUser, updateNoticeStatus} from "../api/noticeApi.ts";
import {getStoredUser} from "../api/apiClient.ts";
import type {User} from "../types/auth.ts";
import type {Notice, NoticeStatus} from "../types/notice.ts";

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
    title1: {
        fontSize: "clamp(28px, 10vw, 48px)",
        fontWeight: "bold",
        letterSpacing: "3px",
        textAlign: "center",
        marginBottom: "10px",
    },
    card: {
        background: "white",
        border: "4px solid #222",
        padding: "clamp(15px, 5vw, 30px)",
        width: "100%",
        maxWidth: "700px",
        boxShadow: "6px 6px 0px #e5e4e7",
        boxSizing: "border-box",
    },
    sectionTitle: {
        fontSize: "20px",
        fontWeight: "bold",
        letterSpacing: "2px",
        marginBottom: "15px",
        borderBottom: "3px solid #222",
        paddingBottom: "8px",
    },
    inputGroup: {
        width: "100%",
        display: "flex",
        flexDirection: "column",
        gap: "8px",
        marginBottom: "15px",
    },
    label: {
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
        fontWeight: "bold",
    },
    input: {
        padding: "12px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        fontSize: "16px",
        outline: "none",
        width: "100%",
        boxSizing: "border-box",
    },
    button: {
        background: "#222",
        color: "white",
        border: "none",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "18px",
        padding: "12px",
        transition: "transform 0.1s",
    },
    noticeList: {
        width: "100%",
        marginBottom: "30px",
        border: "3px solid #222",
        overflowX: "auto",
        WebkitOverflowScrolling: "touch",
    },
    table: {
        width: "100%",
        minWidth: "480px",
        borderCollapse: "collapse",
        fontFamily: '"Space Mono", monospace',
    },
    th: {
        background: "#222",
        color: "#fff",
        padding: "10px 12px",
        textAlign: "left",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "14px",
        fontWeight: "bold",
    },
    td: {
        padding: "10px 12px",
        borderBottom: "2px solid #222",
        fontSize: "13px",
        textAlign: "left",
        background: "#f4f3ec",
    },
    emptyText: {
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        color: "#555",
        marginBottom: "30px",
    },
    errorText: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        marginBottom: "20px",
    },
    actionCell: {
        display: "flex",
        flexDirection: "column",
        gap: "6px",
        minWidth: "120px",
    },
    smallButton: {
        background: "#222",
        color: "white",
        border: "none",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "11px",
        padding: "6px 8px",
        whiteSpace: "nowrap",
    },
    smallSecondaryButton: {
        background: "#fff",
        color: "#222",
        border: "2px solid #222",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "11px",
        padding: "5px 8px",
        whiteSpace: "nowrap",
    },
};

function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

function canManageNotice(notice: Notice): boolean {
    return notice.status === "ACTIVE" || notice.status === "PENDING_MATCH";
}

export default function ProfilePage() {
    const [showModal, setShowModal] = useState(false);
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [statusModalMessage, setStatusModalMessage] = useState("");
    const [passwordError, setPasswordError] = useState("");
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");

    const [notices, setNotices] = useState<Notice[]>([]);
    const [noticesLoading, setNoticesLoading] = useState(true);
    const [noticesError, setNoticesError] = useState("");
    const [statusUpdateError, setStatusUpdateError] = useState("");
    const [updatingNoticeId, setUpdatingNoticeId] = useState<string | null>(null);

    const storedUser = getStoredUser<User>();
    const userId = storedUser?.id;

    const loadNotices = () => {
        if (!userId) return;

        setNoticesLoading(true);
        setNoticesError("");
        setStatusUpdateError("");

        getNoticesByUser(userId)
            .then((all) => setNotices(all.filter((n) => n.type === "LOST")))
            .catch(() => setNoticesError("Failed to load missing pet reports"))
            .finally(() => setNoticesLoading(false));
    };

    useEffect(() => {
        if (!userId) {
            setNoticesLoading(false);
            return;
        }
        loadNotices();
    }, [userId]);

    const handleNoticeStatusChange = async (noticeId: string, status: NoticeStatus) => {
        setUpdatingNoticeId(noticeId);
        setStatusUpdateError("");
        try {
            const updated = await updateNoticeStatus(noticeId, status);
            setNotices((prev) => prev.map((n) => (n.id === noticeId ? updated : n)));
            setStatusModalMessage(
                status === "RESOLVED" ? "REPORT MARKED AS FOUND!" : "REPORT CLOSED!",
            );
            setShowStatusModal(true);
        } catch (error) {
            setStatusUpdateError(error instanceof Error ? error.message : "Failed to update report");
        } finally {
            setUpdatingNoticeId(null);
        }
    };

    const handlePasswordChange = async (e: React.FormEvent) => {
        e.preventDefault();
        setPasswordError("");

        try {
            await changePassword(oldPassword, newPassword);
            setOldPassword("");
            setNewPassword("");
            setShowModal(true);
        } catch (error) {
            setPasswordError(error instanceof Error ? error.message : "Password change failed");
        }
    };

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title1}>USER PROFILE</h1>

                <div style={styles.card}>
                    <div style={{ textAlign: "left", fontFamily: '"Space Mono", monospace', lineHeight: 1.8, marginBottom: "25px" }}>
                        <p><strong>Name:</strong> {storedUser?.firstName} {storedUser?.lastName}</p>
                        <p><strong>Email:</strong> {storedUser?.email}</p>
                        <p><strong>Role:</strong> {storedUser?.roleName}</p>
                    </div>

                    <h2 style={styles.sectionTitle}>REPORTED MISSING ANIMALS</h2>

                    {noticesLoading && (
                        <p style={styles.emptyText}>Loading...</p>
                    )}

                    {!noticesLoading && noticesError && (
                        <p style={styles.errorText}>{noticesError}</p>
                    )}

                    {statusUpdateError && (
                        <p style={styles.errorText}>{statusUpdateError}</p>
                    )}

                    {!noticesLoading && !noticesError && notices.length === 0 && (
                        <p style={styles.emptyText}>No reported missing animals</p>
                    )}

                    {!noticesLoading && !noticesError && notices.length > 0 && (
                        <div style={styles.noticeList}>
                            <table style={styles.table}>
                                <thead>
                                    <tr>
                                        <th style={styles.th}>SPECIES</th>
                                        <th style={styles.th}>BREED</th>
                                        <th style={styles.th}>STATUS</th>
                                        <th style={styles.th}>EVENT DATE</th>
                                        <th style={styles.th}>REPORTED</th>
                                        <th style={styles.th}>ACTIONS</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {notices.map((notice) => (
                                        <tr key={notice.id}>
                                            <td style={styles.td}>{notice.species}</td>
                                            <td style={styles.td}>{notice.breed || "—"}</td>
                                            <td style={styles.td}>{notice.status}</td>
                                            <td style={styles.td}>{formatDate(notice.eventDate)}</td>
                                            <td style={styles.td}>{formatDate(notice.createdAt)}</td>
                                            <td style={styles.td}>
                                                {canManageNotice(notice) ? (
                                                    <div style={styles.actionCell}>
                                                        <button
                                                            type="button"
                                                            style={styles.smallButton}
                                                            disabled={updatingNoticeId === notice.id}
                                                            onClick={() => handleNoticeStatusChange(notice.id, "RESOLVED")}
                                                        >
                                                            MARK FOUND
                                                        </button>
                                                        <button
                                                            type="button"
                                                            style={styles.smallSecondaryButton}
                                                            disabled={updatingNoticeId === notice.id}
                                                            onClick={() => handleNoticeStatusChange(notice.id, "CLOSED")}
                                                        >
                                                            CLOSE
                                                        </button>
                                                    </div>
                                                ) : (
                                                    "—"
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    <h2 style={styles.sectionTitle}>CHANGE PASSWORD</h2>

                    <form onSubmit={handlePasswordChange}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>CURRENT PASSWORD</label>
                            <input
                                type="password"
                                style={styles.input}
                                required
                                value={oldPassword}
                                onChange={(e) => setOldPassword(e.target.value)}
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>NEW PASSWORD</label>
                            <input
                                type="password"
                                style={styles.input}
                                required
                                minLength={8}
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
                        </div>

                        {passwordError && (
                            <p style={{ color: "#ff4d4d", fontFamily: '"Space Mono", monospace', fontSize: "14px" }}>
                                {passwordError}
                            </p>
                        )}

                        <button type="submit" style={{ ...styles.button, width: "100%", marginTop: "10px" }}>
                            UPDATE PASSWORD
                        </button>
                    </form>
                </div>
            </main>

            <PopUp
                show={showModal}
                onClose={() => setShowModal(false)}
                message="PASSWORD UPDATED SUCCESSFULLY!"
                title="SUCCESS"
            />

            <PopUp
                show={showStatusModal}
                onClose={() => setShowStatusModal(false)}
                message={statusModalMessage}
                title="SUCCESS"
            />
        </div>
    );
}

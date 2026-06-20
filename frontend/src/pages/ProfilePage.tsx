import React, {useState, useEffect} from 'react';
import PopUp from '../components/PopUp';
import {changePassword} from "../api/authApi.ts";
import {getNoticesByUser} from "../api/noticeApi.ts";
import {getStoredUser} from "../api/apiClient.ts";
import type {User} from "../types/auth.ts";
import type {Notice} from "../types/notice.ts";

const styles: Record<string, React.CSSProperties> = {
    page: {
        minHeight: "100vh",
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
        maxWidth: "550px",
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
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        marginBottom: "30px",
    },
    noticeItem: {
        border: "3px solid #222",
        padding: "12px",
        background: "#f4f3ec",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        lineHeight: 1.6,
    },
    noticeType: {
        fontWeight: "bold",
        letterSpacing: "1px",
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
};

function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString("pl-PL", {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

export default function ProfilePage() {
    const [showModal, setShowModal] = useState(false);
    const [passwordError, setPasswordError] = useState("");
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");

    const [notices, setNotices] = useState<Notice[]>([]);
    const [noticesLoading, setNoticesLoading] = useState(true);
    const [noticesError, setNoticesError] = useState("");

    const storedUser = getStoredUser<User>();
    const userId = storedUser?.id;

    useEffect(() => {
        if (!userId) {
            setNoticesLoading(false);
            return;
        }

        setNoticesLoading(true);
        setNoticesError("");

        getNoticesByUser(userId)
            .then(setNotices)
            .catch(() => setNoticesError("Nie udało się załadować zgłoszeń"))
            .finally(() => setNoticesLoading(false));
    }, [userId]);

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

                    <h2 style={styles.sectionTitle}>MOJE ZGŁOSZENIA</h2>

                    {noticesLoading && (
                        <p style={styles.emptyText}>Ładowanie...</p>
                    )}

                    {!noticesLoading && noticesError && (
                        <p style={styles.errorText}>{noticesError}</p>
                    )}

                    {!noticesLoading && !noticesError && notices.length === 0 && (
                        <p style={styles.emptyText}>Brak zgłoszeń</p>
                    )}

                    {!noticesLoading && !noticesError && notices.length > 0 && (
                        <div style={styles.noticeList}>
                            {notices.map((notice) => (
                                <div key={notice.id} style={styles.noticeItem}>
                                    <div style={styles.noticeType}>
                                        {notice.type === "LOST" ? "ZAGINIONE" : "ZNALEZIONE"}
                                    </div>
                                    <div><strong>Gatunek:</strong> {notice.species}</div>
                                    {notice.breed && <div><strong>Rasa:</strong> {notice.breed}</div>}
                                    <div><strong>Status:</strong> {notice.status}</div>
                                    <div><strong>Data:</strong> {formatDate(notice.createdAt)}</div>
                                </div>
                            ))}
                        </div>
                    )}

                    <h2 style={styles.sectionTitle}>ZMIEŃ HASŁO</h2>

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
        </div>
    );
}

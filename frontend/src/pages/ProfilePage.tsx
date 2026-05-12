import React, {useState, useRef, useEffect} from 'react';
import PopUp from '../components/PopUp';
import {changePassword, getUserInfo, uploadUserImage} from "../api/authApi.ts";
import cat from '../assets/cat6.jpg';

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
        maxWidth: "450px",
        boxShadow: "6px 6px 0px #e5e4e7",
        boxSizing: "border-box",
    },
    avatarSection: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "15px",
        marginBottom: "30px",
    },
    avatarCircle: {
        width: "120px",
        height: "120px",
        border: "4px solid #222",
        borderRadius: "0",
        background: "#f4f3ec",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        overflow: "hidden",
        boxShadow: "6px 6px 0px #f4f3ec",
    },
    profileImage: {
        width: "100%",
        height: "100%",
        objectFit: "cover",
        imageRendering: "pixelated",
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
    }
};

export default function ProfilePage() {
    const [preview, setPreview] = useState<string | null>(null);
    const [userImage, setUserImage] = useState<string | null>(null);

    const [showModal, setShowModal] = useState(false);

    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");

    const fileInputRef = useRef<HTMLInputElement>(null);

    //const userId = getUserIdFromToken();
    const userId = 2;

    useEffect(() => {
        const loadUser = async () => {
            if (!userId) return;

            try {
                const user = await getUserInfo(userId);
                setUserImage(user.image);
            } catch (error) {
                console.error("Failed to load user:", error);
            }
        };

        loadUser();
    }, [userId]);

    const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file || !userId) return;

        setPreview(URL.createObjectURL(file));

        try {
            const imageUrl = await uploadUserImage(userId, file);

            setUserImage(imageUrl);
        } catch (error) {
            console.error("Upload failed:", error);
        }
    };

    const handlePasswordChange = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!userId) return;

        try {
            await changePassword(userId, oldPassword, newPassword);

            setOldPassword("");
            setNewPassword("");
            setShowModal(true);

        } catch (error) {
            console.error("Password change failed:", error);
        }
    };

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title1}>USER PROFILE</h1>

                <div style={styles.card}>

                    <div style={styles.avatarSection}>
                        <div style={styles.avatarCircle}>
                            {preview || userImage ? (
                                <img src={preview || userImage || cat} style={styles.profileImage} alt="Profile"/>
                            ) : (
                                <span style={{ fontSize: "40px" }}>?</span>
                            )}
                        </div>

                        <input type="file" ref={fileInputRef} onChange={handleImageChange}
                            style={{ display: "none" }} accept="image/*"/>

                        <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            style={{
                                ...styles.button,
                                fontSize: "14px",
                                padding: "8px 15px",
                                background: "#f0f0f0",
                                color: "#222",
                                border: "2px solid #222",
                            }}>
                            CHOOSE PHOTO
                        </button>
                    </div>

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
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
                        </div>

                        <button type="submit" style={{ ...styles.button, width: "100%", marginTop: "10px" }}>
                            UPDATE PASSWORD
                        </button>
                    </form>
                </div>
            </main>

            <PopUp
                show={showModal}
                onClose={() => setShowModal(false)}
                message="PROFILE UPDATED SUCCESSFULLY!"
                title="SUCCESS"
            />
        </div>
    );
}
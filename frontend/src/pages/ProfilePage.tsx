import React, { useState, useRef } from 'react';
import PopUp from '../components/PopUp';


const styles: Record<string, React.CSSProperties> = {
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
        boxShadow: "6px 6px 0px #aa3bff",
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
        fontSize: "18px",
        fontWeight: "bold",
    },
    input: {
        padding: "10px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        outline: "none",
    },
    card: {
        background: "white",
        border: "4px solid #222",
        padding: "30px",
        width: "100%",
        maxWidth: "500px",
        boxShadow: "8px 8px 0px #e5e4e7",
    }
};

export default function ProfilePage() {
    const [preview, setPreview] = useState<string | null>(null);
    const [showModal, setShowModal] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setPreview(URL.createObjectURL(file));
        }
    };

    const handlePasswordChange = (e: React.FormEvent) => {
        e.preventDefault();
        //api
        setShowModal(true);
    };

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title1}>USER PROFILE</h1>

                <div style={styles.card}>
                    <div style={styles.avatarSection}>
                        <div style={styles.avatarCircle}>
                            {preview ? (
                                <img src={preview} style={styles.profileImage} alt="Profile" />
                            ) : (
                                <span style={{fontSize: "40px"}}>?</span>
                            )}
                        </div>
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleImageChange}
                            style={{display: "none"}}
                            accept="image/*"
                        />
                        <button
                            onClick={() => fileInputRef.current?.click()}
                            style={{...styles.button, fontSize: "14px", padding: "5px 10px"}}
                        >
                            CHOOSE PHOTO
                        </button>
                    </div>

                    <form onSubmit={handlePasswordChange}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>CURRENT PASSWORD</label>
                            <input type="password" style={styles.input} required />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>NEW PASSWORD</label>
                            <input type="password" style={styles.input} required />
                        </div>

                        <button type="submit" style={{...styles.button, width: "100%", marginTop: "10px"}}>
                            UPDATE PASSWORD
                        </button>
                    </form>
                </div>
            </main>

            <PopUp
                show={showModal}
                onClose={() => setShowModal(false)}
                message="PROFILE UPDATED SUCCESSFULLY!"
                title={"SUCCESS"}
            />
        </div>
    );
}
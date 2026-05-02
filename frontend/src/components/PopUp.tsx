import React from 'react';

interface PopUpProps {
    show: boolean;
    onClose: () => void;
    title?: string;
    message: string;
    image?: string;
}

const modalStyles: Record<string, React.CSSProperties> = {
    overlay: {
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0,0,0,0.8)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 9999,
        padding: "20px",
    },
    box: {
        background: "#ffffff",
        border: "4px solid #222",
        padding: "30px",
        textAlign: "center",
        maxWidth: "400px",
        width: "100%",
        boxShadow: "10px 10px 0px #aa3bff",
        display: "flex",
        flexDirection: "column",
        gap: "20px",
    },
    title: {
        fontSize: "32px",
        margin: 0,
        color: "black",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    text: {
        fontFamily: '"Space Mono", monospace',
        fontSize: "16px",
        margin: 0,
        color: "#222",
    },
    button: {
        padding: "12px 20px",
        background: "#222",
        color: "white",
        border: "none",
        borderRadius: "4px",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "18px",
        boxShadow: "0px 4px 0px #555",
    }
};

export default function PopUp({ show, onClose, title, message, image }: PopUpProps) {
    if (!show) return null;

    return (
        <div style={modalStyles.overlay}>
            <div style={modalStyles.box}>
                <h2 style={modalStyles.title}>{title}</h2>

                {image && (
                    <div style={{ display: "flex", justifyContent: "center" }}>
                        <img src={image} style={{ width: "100px", imageRendering: "pixelated" }} alt="PopUp icon" />
                    </div>
                )}

                <p style={modalStyles.text}>{message}</p>

                <button onClick={onClose} style={modalStyles.button}>
                    OK -{">"}
                </button>
            </div>
        </div>
    );
}
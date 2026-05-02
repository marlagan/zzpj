import { useState } from "react";
import { login } from "../api/authApi";

const styles: Record<string, React.CSSProperties> = {
    page: {
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        background: "#ffffff",
        color: "#000000",
        fontFamily: '"Pixelify Sans", sans-serif',
        overflow: "hidden",
    },
    main: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: "10px",
        gap: "20px",
        overflowY: "auto",
    },
    title: {
        fontSize: "48px",
        fontWeight: "bold",
        letterSpacing: "4px",
        marginBottom: "10px",
    },
    form: {
        display: "flex",
        flexDirection: "column",
        width: "100%",
        maxWidth: "400px",
        gap: "15px",
    },
    inputGroup: {
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
        gap: "5px",
    },
    label: {
        fontSize: "14px",
        fontWeight: "bold",
    },
    input: {
        width: "100%",
        padding: "12px",
        border: "2px solid #222",
        borderRadius: "4px",
        fontFamily: '"Space Mono", monospace',
        fontSize: "16px",
        boxSizing: "border-box",
    },
    submitButton: {
        marginTop: "10px",
        padding: "15px",
        background: "#222",
        color: "white",
        border: "none",
        borderRadius: "6px",
        cursor: "pointer",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "20px",
    },
    errorText: {
        color: "#ff4d4d",
        fontFamily: '"Space Mono", monospace',
        fontSize: "14px",
        marginTop: "10px",
    }
};

export default function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");

        try {
            const token = await login({ email, password });
            localStorage.setItem("token", token);
            window.location.href = "/";
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("Login failed");
            }
        }
    };

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title}>WELCOME BACK</h1>

                <form style={styles.form} onSubmit={handleSubmit}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>EMAIL</label>
                        <input
                            type="email"
                            placeholder="cat@purrsuit.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            style={styles.input}
                            required
                        />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>PASSWORD</label>
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            style={styles.input}
                            required
                        />
                    </div>

                    <button type="submit" style={styles.submitButton}>
                        LOGIN
                    </button>
                </form>

                {error && <p style={styles.errorText}>{error}</p>}

                <p style={{ fontSize: "14px", marginTop: "20px" }}>
                    New here? <a href="/register" style={{ color: "#aa3bff" }}>Create an account</a>
                </p>
            </main>
        </div>
    );
}
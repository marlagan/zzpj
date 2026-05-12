import React, {useState} from 'react';
import {register} from "../api/authApi.ts";
import cat from "../assets/cat8.jpg";
import catHappy from "../assets/cat7.jpg";
import PopUp from "../components/PopUp.tsx";

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
        padding: "20px",
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
        fontFamily: '"Pixelify Sans", sans-serif',
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
    }
};

export default function RegisterPage() {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [lastName, setLastName] = useState("");
    const [firstName, setFirstName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [error, setError] = useState("");
    const [isRegistered, setIsRegistered] = useState(false);
    const [isNotRegistered, setIsNotRegistered] = useState(false);


    const handleRegistration = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");

        try {
                await register({ email, password, phoneNumber, firstName, lastName });
                setIsRegistered(true);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("Registration failed");
            }
            setIsNotRegistered(true)
        }
    };

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title}>JOIN THE PACK</h1>

                <form style={styles.form} onSubmit={handleRegistration}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>FIRST NAME *</label>
                        <input type="firstNamer"
                               value={firstName}
                               onChange={(e) => setFirstName(e.target.value)}
                               style={styles.input} required placeholder="Enter first name" />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>LAST NAME *</label>
                        <input type="lastName"
                               value={lastName}
                               onChange={(e) => setLastName(e.target.value)}
                               style={styles.input}
                               required placeholder="Enter last name" />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>PHONE NUMBER *</label>
                        <input type="phoneNumber"
                               value={phoneNumber}
                               onChange={(e) => setPhoneNumber(e.target.value)}
                               style={styles.input} required placeholder="123-456-789" />
                    </div>


                    <div style={styles.inputGroup}>
                        <label style={styles.label}>EMAIL *</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            style={styles.input} required placeholder="cat@purrsuit.com" />
                    </div>


                    <div style={styles.inputGroup}>
                        <label style={styles.label}>PASSWORD * 8 characters min</label>
                        <input type="password"
                               value={password}
                               onChange={(e) => setPassword(e.target.value)}
                               style={styles.input} required placeholder="••••••••" />
                    </div>

                    <button type="submit" style={styles.submitButton}>
                        REGISTER
                    </button>
                </form>

                {error && <p style={styles.errorText}>{error}</p>}
                <p style={{ fontFamily: '"Pixelify Sans", sans-serif', fontSize: "14px", marginTop: "20px" }}>
                    Already have an account? <a href="/login">Login here</a>
                </p>
            </main>
            <PopUp
                show={isRegistered}
                onClose={() => window.location.href = "/login"}
                message="WELCOME TO THE PACK! YOUR ACCOUNT IS READY."
                image={catHappy}
                title={"SUCCESS"}
            />
            <PopUp
                show={isNotRegistered}
                onClose={() => window.location.href = "/register"}
                message="REGISTRATION FAILURE."
                image={cat}
                title="FAILURE"
            />
        </div>
    );
}
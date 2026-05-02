import catGif from "../assets/cat_gif.gif";
import {Link} from "react-router-dom";

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
        padding: "20px",
        gap: "30px",
        overflowY: "auto",
    },
    title: {
        fontSize: "64px",
        fontWeight: "bold",
        letterSpacing: "8px",
        margin: "0",
        color: "black",
        textShadow: "4px 4px 0px #222",
        textAlign: "center",
    },
    section: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "15px",
        padding: "20px",
        width: "280px",
        border: "3px solid #222",
        borderRadius: "8px",
        background: "#f4f3ec",
        boxShadow: "6px 6px 0px #222",
    },
    sectionText: {
        textAlign: "center",
        fontSize: "18px",
        margin: 0,
        lineHeight: "1.2",
        fontFamily: '"Space Mono", monospace',
        fontWeight: "bold",
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
        width: "100%",
        boxShadow: "0px 4px 0px #555",
    },
    catImage: {
        width: "350px",
        height: "auto",
        imageRendering: "pixelated",
    },
    imageContainer: {
        width: "100%",
        display: "flex",
        justifyContent: "center",
        marginBottom: "-10px",
    }
};

export default function MainPage() {
    const token = localStorage.getItem("token");
    return (
        <div style={styles.page}>
            <main style={styles.main}>

                <div style={styles.imageContainer}>
                    <img src={catGif} style={styles.catImage} alt="jumping cat animation"/>
                </div>

                <h1 style={styles.title}>PURRSUIT</h1>

                <div style={{ display: "flex", gap: "25px", flexWrap: "wrap", justifyContent: "center" }}>

                    <div style={styles.section}>
                        {!token ? (
                        <Link to="/login">
                            <p style={styles.sectionText}>REPORT A MISSING<br/>CAT/DOG</p>
                            <button style={styles.button}>GO</button>
                        </Link>
                            ):(
                                <>
                                <p style={styles.sectionText}>REPORT A MISSING<br/>CAT/DOG</p>
                                 <button style={styles.button}>GO</button>
                                </>
                            )}
                    </div>

                    <div style={styles.section}>
                        {!token ? (
                        <Link to="/login">
                            <p style={styles.sectionText}>SAW A STRAY<br/>CAT/DOG?</p>
                            <button style={styles.button}>HELP</button>
                        </Link>
                            ):(
                                <>
                                    <p style={styles.sectionText}>SAW A STRAY<br/>CAT/DOG?</p>
                                    <button style={styles.button}>HELP</button>
                                </>
                            )}
                    </div>

                </div>

                <p style={{ marginTop: "20px", fontSize: "14px", opacity: 0.6 }}>
                    HELPING PAWS FIND THEIR WAY HOME
                </p>
            </main>
        </div>
    );
}
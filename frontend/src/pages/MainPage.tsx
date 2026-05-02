import catGif from "../assets/cat_gif.gif";

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
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    section: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "10px",
        marginBottom: "10px",
    },
    button: {
        padding: "10px 18px",
        background: "#222",
        color: "white",
        border: "none",
        borderRadius: "6px",
        cursor: "pointer",
    },
    box: {
        width: "80px",
        height: "80px",
        background: "#eee",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "12px",
    },
    catImage: {
        width: "80%",
        maxWidth: "450px",
        height: "auto",
        display: "block",
    },
    imageContainer: {
        width: "100%",
        display: "flex",
        justifyContent: "center",
    }
};

export default function MainPage() {
    return (
        <div style={styles.page}>

            <main style={styles.main}>

                <div style={styles.imageContainer}>
                    <img src={catGif} style={styles.catImage} alt="jumping cat animation"/>
                </div>

                <h1 style={styles.title}>PURRSUIT</h1>

                <div style={styles.section}>
                    <p>REPORT A MISSING<br/>CAT/DOG</p>
                    <button style={styles.button}>Button</button>
                </div>
                <div style={styles.section}>
                    <p>SAW A STRAY<br/>CAT/DOG?</p>
                    <button style={styles.button}>Button</button>
                </div>
            </main>
        </div>
    );
}
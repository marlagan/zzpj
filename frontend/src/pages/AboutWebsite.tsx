import catGif from "../assets/cat5.jpg";

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
        padding: "40px 20px",
        overflowY: "auto",
        gap: "10px",
    },
    title1: {
        fontSize: "52px",
        fontWeight: "bold",
        letterSpacing: "6px",
        textAlign: "center",
        margin: "0",
        color: "black",
    },
    title2: {
        fontSize: "24px",
        fontWeight: "500",
        letterSpacing: "2px",
        textAlign: "center",
        margin: "0 0 10px 0",
        background: "#222",
        color: "#fff",
        padding: "4px 12px",
        borderRadius: "4px",
    },
    missionBox: {
        maxWidth: "600px",
        padding: "20px",
        border: "3px solid #222",
        borderRadius: "8px",
        background: "#f4f3ec",
        boxShadow: "4px 4px 0px #e5e4e7",
        textAlign: "center",
        lineHeight: "1.6",
        fontFamily: '"Space Mono", monospace',
        fontSize: "16px",
        marginBottom: "20px",
    },
    catImage: {
        width: "120px",
        height: "auto",
        imageRendering: "pixelated",
    },
    imageContainer: {
        width: "100%",
        display: "flex",
        justifyContent: "center",
        marginTop: "10px",
    }
};

export default function AboutWebsite() {
    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title1}>PURRSUIT</h1>
                <h2 style={styles.title2}>OUR MISSION</h2>

                <div style={styles.missionBox}>
                    <p style={{ margin: 0 }}>
                        At Purrsuit, we believe that every lost paw deserves a way home.
                        Our mission is to build a bridge between missing pets and
                        compassionate finders using simple, pixel-perfect technology.
                        <br/><br/>
                        Helping communities reunite with their furry friends,
                        one pixel at a time.
                    </p>
                </div>

                <div style={styles.imageContainer}>
                    <img
                        src={catGif}
                        style={styles.catImage}
                        alt="jumping cat animation"
                    />
                </div>
            </main>
        </div>
    );
}
import envelope from "../assets/envelope.jpg";
import catGif from "../assets/cat4Gif.gif";

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
        gap: "15px",
        overflowY: "auto",
    },
    title1: {
        fontSize: "36px",
        fontWeight: "bold",
        letterSpacing: "3px",
        textAlign: "center",
        margin: 0,
        color: "black",
    },
    title2: {
        fontSize: "48px",
        fontWeight: "bold",
        letterSpacing: "4px",
        textAlign: "center",
        margin: "0 0 10px 0",
        color: "black",
    },
    emailContainer: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "15px",
        padding: "15px 25px",
        background: "#f4f3ec",
        borderRadius: "4px",
        border: "3px solid #222",
        boxShadow: "5px 5px 0px #e5e4e7",
        marginTop: "20px",
    },
    contactText: {
        fontSize: "20px",
        fontFamily: '"Space Mono", monospace',
        fontWeight: "bold",
        color: "#222",
        margin: 0,
    },
    catImage: {
        width: "140px",
        height: "auto",
        imageRendering: "pixelated",
    },
    imageContainer: {
        width: "100%",
        display: "flex",
        justifyContent: "center",
        margin: "10px 0",
    },
    envelopeImage: {
        width: "35px",
        height: "auto",
        imageRendering: "pixelated",
    }
};

export default function ContactPage() {
    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title1}>DO YOU WANT TO</h1>
                <h1 style={styles.title2}>CONTACT US?</h1>

                <div style={styles.imageContainer}>
                    <img src={catGif} style={styles.catImage} alt="jumping cat animation"/>
                </div>

                <div style={styles.emailContainer}>
                    <img src={envelope} alt="envelope" style={styles.envelopeImage}/>
                    <p style={styles.contactText}>purrsuit@gmail.com </p>
                </div>

                <p style={{marginTop: "20px", fontSize: "14px", fontFamily: '"Space Mono", monospace', opacity: 0.7}}>
                    WE USUALLY REPLY WITHIN 24 PAWS
                </p>
            </main>
        </div>
    );
}
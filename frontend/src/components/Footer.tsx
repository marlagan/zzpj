import { Link } from "react-router-dom";

const styles: Record<string, React.CSSProperties> = {
    nav: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "16px",
        backgroundColor: "#9BA3A8",
        borderBottom: "1px solid #ddd",
    },
    links: {
        display: "flex",
        gap: "20px",
        alignItems: "center",
    },
    link: {
        textDecoration: "none",
        color: "black",
        fontFamily: "TIm"
    },

};

export default function Footer() {
    return (
        <nav style={styles.nav}>
            <div style={styles.links}>
                <Link to="/about-website" style={styles.link}>about website</Link>
                <Link to="/contact" style={styles.link}>contact</Link>
            </div>
        </nav>
    );
}
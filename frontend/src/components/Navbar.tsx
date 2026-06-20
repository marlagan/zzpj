import { Link, useLocation } from "react-router-dom";
import { clearAuth, getStoredUser } from "../api/apiClient";
import type { User } from "../types/auth";

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
    },
};

export default function Navbar() {
    const token = localStorage.getItem("token");
    const user = getStoredUser<User>();
    const location = useLocation();

    const handleLogout = () => {
        clearAuth();
        window.location.href = "/";
    };

    return (
        <nav style={styles.nav}>
            <div style={styles.links}>
                {location.pathname !== "/" && (
                    <Link to="/" style={styles.link}>Home</Link>
                )}

                {!token ? (
                    <>
                        <Link to="/login" style={styles.link}>Login</Link>
                        <Link to="/register" style={styles.link}>Register</Link>
                    </>
                ) : (
                    <>
                        <Link to="/profile" style={styles.link}>Profile</Link>
                        {user?.roleName === "ADMIN" && (
                            <Link to="/admin" style={styles.link}>Admin</Link>
                        )}
                        <Link to="/" onClick={handleLogout} style={styles.link}>Logout</Link>
                    </>
                )}
            </div>
        </nav>
    );
}

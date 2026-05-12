import React, { useState } from 'react';
import PopUp from '../components/PopUp';

const styles: Record<string, React.CSSProperties> = {
    page: {
        minHeight: "100vh", // Changed from height to minHeight
        display: "flex",
        flexDirection: "column",
        background: "#ffffff",
        color: "#000000",
        fontFamily: '"Pixelify Sans", sans-serif',
    },
    main: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "flex-start", // Start at top for better mobile scrolling
        padding: "20px 10px",
        gap: "20px",
    },
    title: {
        fontSize: "clamp(24px, 8vw, 48px)", // Fluid typography for mobile
        fontWeight: "bold",
        letterSpacing: "2px",
        textAlign: "center",
        marginBottom: "10px",
    },
    // Added missing button style
    button: {
        fontFamily: '"Pixelify Sans", sans-serif',
        cursor: "pointer",
        border: "2px solid #222",
        borderRadius: "4px",
        background: "#eee",
    }
};

const adminStyles: Record<string, React.CSSProperties> = {
    searchContainer: {
        width: "100%",
        maxWidth: "500px",
        marginBottom: "20px",
        padding: "0 10px",
        boxSizing: "border-box"
    },
    input: {
        width: "100%",
        padding: "12px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        boxShadow: "4px 4px 0px #f4f3ec",
        outline: "none",
        boxSizing: "border-box"
    },
    tableContainer: {
        width: "95%",
        maxWidth: "800px",
        border: "4px solid #222",
        boxShadow: "6px 6px 0px #f4f3ec",
        background: "#fff",
        overflowX: "auto",
        WebkitOverflowScrolling: "touch",
    },
    table: {
        width: "100%",
        minWidth: "600px",
        borderCollapse: "collapse",
        fontFamily: '"Space Mono", monospace',
    },
    th: {
        background: "#222",
        color: "#fff",
        padding: "12px",
        textAlign: "left",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "16px",
    },
    td: {
        padding: "12px 10px",
        borderBottom: "2px solid #222",
        color: "#222",
        fontSize: "14px"
    },
    badgeAdmin: {
        background: "#222",
        color: "white",
        padding: "2px 8px",
        fontSize: "12px",
        fontWeight: "bold",
    },
    actionsCell: {
        display: "flex",
        gap: "5px",
        flexWrap: "wrap"
    }
};

const MOCK_USERS = [
    { id: 1, email: "admin@purrsuit.com", roleName: "ADMIN"},
    { id: 2, email: "user1@gmail.com", roleName: "USER"},
    { id: 3, email: "cat_lover@wp.pl", roleName: "USER"},
];

export default function AdminPage() {
    const [users, setUsers] = useState(MOCK_USERS);
    const [search, setSearch] = useState("");
    const [showModal, setShowModal] = useState(false);

    const deleteUser = (id: number) => {
        if (window.confirm("ARE YOU SURE YOU WANT TO DELETE THIS USER?")) {
            setUsers(users.filter(u => u.id !== id));
            setShowModal(true);
        }
    };

    const toggleRole = (id: number) => {
        setUsers(users.map(u => {
            if (u.id === id) {
                return { ...u, roleName: u.roleName === "ADMIN" ? "USER" : "ADMIN" };
            }
            return u;
        }));
    };

    const filteredUsers = users.filter(u => u.email.toLowerCase().includes(search.toLowerCase()));

    return (
        <div style={styles.page}>
            <main style={styles.main}>
                <h1 style={styles.title}>ADMIN PANEL</h1>

                <div style={adminStyles.searchContainer}>
                    <input
                        style={adminStyles.input}
                        placeholder="SEARCH BY EMAIL..."
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>

                <div style={adminStyles.tableContainer}>
                    <table style={adminStyles.table}>
                        <thead>
                        <tr>
                            <th style={adminStyles.th}>EMAIL</th>
                            <th style={adminStyles.th}>ROLE</th>
                            <th style={adminStyles.th}>ACTIONS</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredUsers.map(user => (
                            <tr key={user.id}>
                                <td style={adminStyles.td}>{user.email}</td>
                                <td style={adminStyles.td}>
                                        <span style={user.roleName === "ADMIN" ? adminStyles.badgeAdmin : { border: "1px solid #222", padding: "2px 8px" }}>
                                            {user.roleName}
                                        </span>
                                </td>
                                <td style={adminStyles.td}>
                                    <div style={adminStyles.actionsCell}>
                                        <button
                                            onClick={() => toggleRole(user.id)}
                                            style={{...styles.button, padding: "8px", fontSize: "10px"}}
                                        >
                                            ROLES
                                        </button>
                                        <button
                                            onClick={() => deleteUser(user.id)}
                                            style={{...styles.button, background: "#ff4444", color: "white", padding: "8px", fontSize: "10px"}}
                                        >
                                            DEL
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </main>

            <PopUp
                show={showModal}
                onClose={() => setShowModal(false)}
                message="USER DATABASE UPDATED!"
                title="SUCCESS"
            />
        </div>
    );
}
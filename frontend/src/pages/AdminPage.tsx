import React, { useState } from 'react';
import PopUp from '../components/PopUp';

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

const adminStyles: Record<string, React.CSSProperties> = {
    searchContainer: {
        width: "100%",
        maxWidth: "500px",
        marginBottom: "20px",
    },
    input: {
        width: "100%",
        padding: "12px",
        border: "3px solid #222",
        fontFamily: '"Space Mono", monospace',
        boxShadow: "4px 4px 0px #aa3bff",
        outline: "none",
    },
    tableContainer: {
        width: "100%",
        maxWidth: "800px",
        border: "4px solid #222",
        boxShadow: "8px 8px 0px #e5e4e7",
        background: "#fff",
        overflowX: "auto",
    },
    table: {
        width: "100%",
        borderCollapse: "collapse",
        fontFamily: '"Space Mono", monospace',
    },
    th: {
        background: "#222",
        color: "#fff",
        padding: "15px",
        textAlign: "left",
        fontFamily: '"Pixelify Sans", sans-serif',
        fontSize: "18px",
    },
    td: {
        padding: "12px 15px",
        borderBottom: "2px solid #222",
        color: "#222",
    },
    badgeAdmin: {
        background: "#aa3bff",
        color: "black",
        padding: "2px 8px",
        fontSize: "12px",
        fontWeight: "bold",
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
                <h1 style={styles.title1}>ADMIN PANEL</h1>

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
                                        <span style={user.roleName === "ADMIN" ? adminStyles.badgeAdmin : {}}>
                                            {user.roleName}
                                        </span>
                                </td>
                                <td style={adminStyles.td}>
                                    <button
                                        onClick={() => toggleRole(user.id)}
                                        style={{...styles.button, padding: "5px 10px", fontSize: "12px", marginRight: "5px"}}
                                    >
                                        PROMOTE/DEMOTE
                                    </button>
                                    <button
                                        onClick={() => deleteUser(user.id)}
                                        style={{...styles.button, background: "#ff4444", padding: "5px 10px", fontSize: "12px"}}
                                    >
                                        DELETE
                                    </button>
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
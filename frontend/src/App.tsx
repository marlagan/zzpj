import React from "react";
import { Routes, Route, useLocation } from "react-router-dom";
import Login from "./pages/LoginPage";

import MainPage from "./pages/MainPage.tsx";
import Navbar from "./components/Navbar.tsx";
import Footer from "./components/Footer.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import ContactPage from "./pages/ContactPage.tsx";
import AboutWebsite from "./pages/AboutWebsite.tsx";
import ProfilePage from "./pages/ProfilePage.tsx";
import AdminPage from "./pages/AdminPage.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import CreateNoticePage from "./pages/CreateNoticePage.tsx";
import NoticeDetailPage from "./pages/NoticeDetailPage.tsx";
import NoticesListPage from "./pages/NoticesListPage.tsx";
import MapPage from "./pages/MapPage.tsx";

function App() {
    const location = useLocation();
    const isMapPage = location.pathname === "/map";

    const contentAreaStyle: React.CSSProperties = {
        flex: 1,
        minHeight: 0,
        overflowY: isMapPage ? "hidden" : "auto",
        overflowX: "hidden",
        display: "flex",
        flexDirection: "column",
        width: "100%",
        alignSelf: "stretch",
    };

    return (
        <>
        <Navbar/>
        <div style={contentAreaStyle}>
            <Routes>
                <Route path="/" element={<MainPage/>} />
                <Route path="/login" element={<Login/>} />
                <Route path="/register" element={<RegisterPage/>} />
                <Route path="/contact" element={<ContactPage/>} />
                <Route path="/about-website" element={<AboutWebsite/>} />
                <Route path="/admin" element={
                    <ProtectedRoute requireAdmin>
                        <AdminPage/>
                    </ProtectedRoute>
                } />
                <Route path="/profile" element={
                    <ProtectedRoute>
                        <ProfilePage/>
                    </ProtectedRoute>
                } />
                <Route path="/map" element={
                    <ProtectedRoute>
                        <MapPage/>
                    </ProtectedRoute>
                } />
                <Route path="/notices" element={
                    <ProtectedRoute>
                        <NoticesListPage/>
                    </ProtectedRoute>
                } />
                <Route path="/notices/create/:type" element={
                    <ProtectedRoute>
                        <CreateNoticePage/>
                    </ProtectedRoute>
                } />
                <Route path="/notices/:id" element={
                    <ProtectedRoute>
                        <NoticeDetailPage/>
                    </ProtectedRoute>
                } />
            </Routes>
        </div>
        <Footer/>
        </>
    );
}

export default App;
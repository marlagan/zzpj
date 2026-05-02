import { Routes, Route} from "react-router-dom";
import Login from "./pages/LoginPage";

import MainPage from "./pages/MainPage.tsx";
import Navbar from "./components/Navbar.tsx";
import Footer from "./components/Footer.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import ContactPage from "./pages/ContactPage.tsx";
import AboutWebsite from "./pages/AboutWebsite.tsx";

function App() {
    return (
        <>
        <Navbar/>
            <Routes>
                <Route path="/" element={<MainPage/>} />
                <Route path="/login" element={<Login/>} />
                <Route path="/register" element={<RegisterPage/>} />
                <Route path="/contact" element={<ContactPage/>} />
                <Route path="/about-website" element={<AboutWebsite/>} />
            </Routes>
        <Footer/>
        </>
    );
}

export default App;
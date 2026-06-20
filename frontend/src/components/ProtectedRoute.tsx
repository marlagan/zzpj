import { Navigate } from "react-router-dom";
import { getStoredUser } from "../api/apiClient";
import type { User } from "../types/auth";

type Props = {
    children: React.ReactNode;
    requireAdmin?: boolean;
};

export default function ProtectedRoute({ children, requireAdmin = false }: Props) {
    const token = localStorage.getItem("token");
    const user = getStoredUser<User>();

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    if (requireAdmin && user?.roleName !== "ADMIN") {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
}

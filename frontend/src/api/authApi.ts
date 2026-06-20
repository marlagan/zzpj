import { KEYCLOAK_CLIENT_ID, KEYCLOAK_REALM, KEYCLOAK_URL } from "../config";
import type { User, UserLoginDTO, UserRegistrationDTO } from "../types/auth";
import { apiFetch, setToken, storeUser } from "./apiClient";

export async function login(data: UserLoginDTO): Promise<string> {
    const body = new URLSearchParams({
        grant_type: "password",
        client_id: KEYCLOAK_CLIENT_ID,
        username: data.email,
        password: data.password,
    });

    const response = await fetch(
        `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
        {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body,
        }
    );

    if (!response.ok) {
        throw new Error("Invalid email or password");
    }

    const tokenData = await response.json();
    const token = tokenData.access_token as string;
    setToken(token);

    const user = await apiFetch<User>("/api/users/sync-profile", { method: "POST" });
    storeUser(user);

    return token;
}

export async function register(data: UserRegistrationDTO): Promise<string> {
    await apiFetch<string>("/api/users/public/register", {
        method: "POST",
        body: JSON.stringify(data),
    });

    return login({ email: data.email, password: data.password });
}

export const getAllUsers = () => apiFetch<User[]>("/api/users/get-all-users");

export const deleteUserById = (id: string) =>
    apiFetch<string>(`/api/users/${id}`, { method: "DELETE" });

export const changeRoleById = (id: string, roleName: string) =>
    apiFetch<string>(`/api/users/${id}/role?role=${roleName}`, { method: "PATCH" });

export const getUserInfo = (id: string) => apiFetch<User>(`/api/users/users/${id}`);

export const changePassword = (oldPassword: string, newPassword: string) =>
    apiFetch<string>("/api/users/profile/change-password", {
        method: "POST",
        body: JSON.stringify({ oldPassword, newPassword }),
    });

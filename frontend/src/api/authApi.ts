import type { UserRegistrationDTO, UserLoginDTO } from "../types/auth";

const BASE_URL = "http://localhost:8086";

export const register = async (data: UserRegistrationDTO) => {
    const response = await fetch(`${BASE_URL}/register`, {
        method: "POST", 
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.text();
};

export const login = async (data: UserLoginDTO) => {
    const response = await fetch(`${BASE_URL}/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    const token = await response.text();
    return token;
};

export const deleteUserById = async (id: number) => {
    const response = await fetch(`${BASE_URL}/${id}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return true;
};


export const getUserByEmail = async (email: string) => {
    const response = await fetch(`${BASE_URL}/users/email/${email}`, {
        method: "GET",
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.json();
};

export const getAllUsers = async () => {
    const response = await fetch(`${BASE_URL}/get-all-users`, {
        method: "GET",
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.json();
};


export const changeRoleById = async (id: number, roleName: string) => {
    const response = await fetch(`${BASE_URL}/${id}/role?role=${roleName}`, {
        method: "PATCH",
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return true;
};


export const changePassword = async (id: number, oldPassword: string, newPassword: string) => {
    const response = await fetch(`${BASE_URL}/change-password/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            oldPassword,
            newPassword,
        }),
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }
    return await response.text();
};

export const uploadUserImage = async (id: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${BASE_URL}/upload/${id}`, {
        method: "POST",
        body: formData,
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.text();
};

export const getUserInfo = async (id: number) => {
    const response = await fetch(`${BASE_URL}/users/${id}`);

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.json();
};
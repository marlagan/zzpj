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
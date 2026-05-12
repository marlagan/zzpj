export type UserRegistrationDTO = {
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    password: string;
};

export type UserLoginDTO = {
    email: string;
    password: string
}

export type User = {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    roleName: "ADMIN" | "USER";
};


import api from '../axios';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface SignupRequest {
    email: string;
    password: string;
    name: string;
    phoneNumber: string;
    gender: 'MALE' | 'FEMALE';
    address: string;
    role: string;
}

export interface LoginResponse {
    userId: number;
    email: string;
    role: string;
    accessToken: string;
    refreshToken: string;
}

export interface UserResponse {
    id: number;
    email: string;
    name: string;
    phoneNumber: string;
    address?: string;
    gender?: 'MALE' | 'FEMALE';
    role: string;
    isActive: boolean;
    createdAt: string;
}

export interface UpdateProfileRequest {
    name: string;
    phoneNumber: string;
    address?: string;
    gender?: 'MALE' | 'FEMALE';
}

export const login = async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<{ data: LoginResponse }>('/api/auth/login', data);
    return response.data.data;
};

export const signup = async (data: SignupRequest): Promise<void> => {
    await api.post('/api/auth/signup', data);
};

export const getMyProfile = async (): Promise<UserResponse> => {
    const response = await api.get<{ data: UserResponse }>('/api/auth/me');
    return response.data.data;
};

export const updateProfile = async (data: UpdateProfileRequest): Promise<UserResponse> => {
    const response = await api.put<{ data: UserResponse }>('/api/auth/profile', data);
    return response.data.data;
};

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export interface WithdrawRequest {
    password: string;
}

export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
    await api.put('/api/auth/changePassword', data);
};

export const withdraw = async (data: WithdrawRequest): Promise<void> => {
    await api.delete('/api/auth/withdraw', { data });
};

export const logout = async (): Promise<void> => {
    await api.post('/api/auth/logout');
};

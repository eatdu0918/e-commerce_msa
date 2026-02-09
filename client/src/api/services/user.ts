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
    role?: 'CUSTOMER' | 'SELLER';
}

export interface LoginResponse {
    userId: number;
    email: string;
    role: string;
    accessToken: string;
    refreshToken: string;
}

export interface UserResponse {
    userId: number;
    email: string;
    username: string;
    role: string;
}

export interface UpdateProfileRequest {
    username?: string;
    phoneNumber?: string;
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
    const response = await api.put<{ data: UserResponse }>('/api/auth/me', data);
    return response.data.data;
};

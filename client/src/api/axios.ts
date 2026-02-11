import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8000',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// Request interceptor to add the auth token header to requests
api.interceptors.request.use(
    (config) => {
        const token = sessionStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle global errors here (e.g., token expiration, 401, 500)
        if (error.response?.status === 401) {
            // Redirect to login or refresh token logic
            console.warn('Unauthorized access. Redirecting to login...');
            sessionStorage.removeItem('accessToken');
            sessionStorage.removeItem('refreshToken');
            // Force redirect to login page (or trigger existing auth flow)
            // Since we use a modal or might want a full redirect:
            window.location.href = '/?login=true';
        }
        return Promise.reject(error);
    }
);

export default api;

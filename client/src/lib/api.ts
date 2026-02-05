import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

export const authService = {
    login: (credentials: any) => api.post('/auth/login', credentials),
    register: (data: any) => api.post('/auth/register', data),
};

export const productService = {
    getProducts: () => api.get('/products'),
    getProduct: (id: string) => api.get(`/products/${id}`),
};

export default api;

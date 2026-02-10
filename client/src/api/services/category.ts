import api from '../axios';

export interface CategoryResponse {
    id: number;
    name: string;
    description: string;
    parentId: number | null;
    children: CategoryResponse[];
    productCount: number;
    createdAt: string;
    updatedAt: string;
}

export const getAllCategories = async (): Promise<CategoryResponse[]> => {
    const response = await api.get<{ data: CategoryResponse[] }>('/api/categories');
    return response.data.data;
};

export const getRootCategories = async (): Promise<CategoryResponse[]> => {
    const response = await api.get<{ data: CategoryResponse[] }>('/api/categories/root');
    return response.data.data;
};

export const getChildCategories = async (parentId: number): Promise<CategoryResponse[]> => {
    const response = await api.get<{ data: CategoryResponse[] }>(`/api/categories/${parentId}/children`);
    return response.data.data;
};

export const getCategory = async (categoryId: number): Promise<CategoryResponse> => {
    const response = await api.get<{ data: CategoryResponse }>(`/api/categories/${categoryId}`);
    return response.data.data;
};

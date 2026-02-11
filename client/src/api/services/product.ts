import api from '../axios';
import type { Product } from '../../types/product';

export interface ProductResponse {
    id: number;
    name: string;
    description: string;
    price: number;
    stockQuantity: number;
    categoryId: number;
    categoryName: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
    imageUrl: string;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// Data Transformer
const transformProduct = (serverProduct: ProductResponse): Product => {
    // Deterministic random rating based on ID
    const rating = 3 + (serverProduct.id % 20) / 10; // 3.0 ~ 4.9
    const reviews = Math.floor(serverProduct.id * 10 + (serverProduct.id % 5) * 100);

    // Map Category to Image (Simple mapping)
    // let image = '/assets/images/product_vase.png'; // Default
    // const catName = serverProduct.categoryName?.toUpperCase() || '';

    // if (catName.includes('CLOTH') || catName.includes('APPAREL')) {
    //     image = '/assets/images/product_hoodie.png';
    //     if (serverProduct.id % 2 === 0) image = '/assets/images/product_cardigan.png';
    // } else if (catName.includes('HOME') || catName.includes('LIVING')) {
    //     image = '/assets/images/product_lamp.png';
    // } else if (catName.includes('FOOT') || catName.includes('SHOE')) {
    //     image = '/assets/images/product_runner.png';
    // } else if (catName.includes('ELECTRONIC')) {
    //     image = '/assets/images/product_headphones.png';
    // }

    return {
        id: serverProduct.id,
        name: serverProduct.name,
        category: serverProduct.categoryName || 'General',
        price: serverProduct.price,
        originalPrice: undefined, // Backend doesn't have it
        date: serverProduct.createdAt.split('T')[0],
        description: serverProduct.description,
        image: serverProduct.imageUrl || '/assets/images/product_vase.png',
        rating: Number(rating.toFixed(1)),
        reviews: reviews,
        badge: serverProduct.stockQuantity < 5 ? 'Low Stock' : undefined,
        discount: undefined
    };
};

export const fetchProducts = async (page = 0, size = 10, categoryId?: number, sort?: string, keyword?: string): Promise<PageResponse<Product>> => {
    const params: any = { page, size };
    if (categoryId) params.categoryId = categoryId;
    if (sort) params.sort = sort;
    if (keyword) params.keyword = keyword;

    const response = await api.get<{ data: PageResponse<ProductResponse> }>('/api/products', { params });
    const pageData = response.data.data;

    return {
        ...pageData,
        content: pageData.content.map(transformProduct)
    };
};

export const getProduct = async (id: number): Promise<Product> => {
    const response = await api.get<{ data: ProductResponse }>(`/api/products/${id}`);
    return transformProduct(response.data.data);
};

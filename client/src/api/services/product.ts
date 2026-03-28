import api from '../axios';
import type { Product } from '../../types/product';
import i18n from '../../i18n';
import { catalogCategoryName, catalogProductDescription, catalogProductName } from '../../lib/catalogLocale';

export interface ProductResponse {
    id: number;
    name: string;
    nameKo?: string | null;
    description: string;
    descriptionKo?: string | null;
    price: number;
    stockQuantity: number;
    categoryId: number;
    categoryName: string;
    categoryNameKo?: string | null;
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

// Data Transformer — 표시명은 현재 i18n 언어에 맞춤
const transformProduct = (serverProduct: ProductResponse): Product => {
    const rating = 3 + (serverProduct.id % 20) / 10; // 3.0 ~ 4.9
    const reviews = Math.floor(serverProduct.id * 10 + (serverProduct.id % 5) * 100);
    const lang = i18n.language;

    const categoryLabel = serverProduct.categoryName
        ? catalogCategoryName(
              { name: serverProduct.categoryName, nameKo: serverProduct.categoryNameKo },
              lang
          )
        : 'General';

    return {
        id: serverProduct.id,
        name: catalogProductName(serverProduct, lang),
        category: categoryLabel,
        price: serverProduct.price,
        originalPrice: undefined,
        date: serverProduct.createdAt.split('T')[0],
        description: catalogProductDescription(serverProduct, lang),
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

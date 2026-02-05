export interface Product {
    id: number;
    name: string;
    price: number;
    originalPrice?: number;
    image: string;
    rating: number;
    reviews: number;
    badge?: string;
    discount?: number;
    category: string;
    description: string;
    date: string;
}


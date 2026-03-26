import type { Product } from '../../types/product';

interface ProductCardProps {
    product: Product;
    timer?: string;
}

export default function ProductCard({ product, timer }: ProductCardProps) {
    return (
        <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-100 relative group overflow-hidden">
            {timer && (
                <div className="absolute top-6 right-6 bg-black text-white px-3 py-1 rounded-full text-[10px] font-bold z-10">
                    {timer}
                </div>
            )}

            <div className="aspect-square overflow-hidden rounded-2xl bg-stone-50 mb-6">
                <img
                    src={product.image}
                    alt={product.name}
                    loading="lazy"
                    decoding="async"
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                />
            </div>

            <div className="space-y-1">
                <p className="text-xs text-stone-400 uppercase tracking-widest">{product.badge}</p>
                <h4 className="font-medium">{product.name}</h4>
                <div className="flex items-center space-x-2 mt-2">
                    <span className="font-bold">${product.price.toLocaleString()}</span>
                    {product.originalPrice && (
                        <span className="text-stone-300 line-through text-sm">${product.originalPrice.toLocaleString()}</span>
                    )}
                </div>
            </div>
        </div>
    );
}

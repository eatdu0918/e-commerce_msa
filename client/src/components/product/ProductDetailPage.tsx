import { useState } from 'react';
import { ChevronLeft, Minus, Plus } from 'lucide-react';
import type { Product } from '../../types/product';
import OrderModal from '../order/OrderModal';
import { useQuery } from '@tanstack/react-query';
import { getMyProfile } from '../../api/services/user';

interface ProductDetailPageProps {
    product: Product;
    onBack: () => void;
    onLoginRequest: () => void;
}

export default function ProductDetailPage({ product, onBack, onLoginRequest }: ProductDetailPageProps) {
    const [selectedSize, setSelectedSize] = useState('S');
    const [quantity, setQuantity] = useState(1);
    const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);

    const { data: user } = useQuery({ queryKey: ['user'], queryFn: getMyProfile, retry: false });

    const sizes = ['S', 'M', 'L'];

    const handleBuyClick = () => {
        if (!user) {
            onLoginRequest();
            return;
        }
        setIsOrderModalOpen(true);
    };

    return (
        <div className="min-h-screen py-16 bg-[#f9f7f2]">
            <div className="max-w-7xl mx-auto px-6">
                <button
                    onClick={onBack}
                    className="flex items-center text-stone-400 hover:text-black transition-colors mb-12 group"
                >
                    <ChevronLeft size={20} className="mr-2 transition-transform group-hover:-translate-x-1" />
                    <span className="text-sm font-medium">돌아가기</span>
                </button>

                <div className="grid md:grid-cols-2 gap-16 items-start">
                    <div className="aspect-[4/5] bg-stone-100 rounded-[40px] overflow-hidden shadow-sm">
                        <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                    </div>

                    <div className="text-left">
                        <p className="text-xs text-stone-400 uppercase tracking-widest mb-2 font-bold">{product.category}</p>
                        <h2 className="text-4xl font-bold mb-4 tracking-tight text-stone-900">{product.name}</h2>
                        <p className="text-2xl font-light mb-8 text-stone-800">₩{product.price.toLocaleString()}</p>
                        <p className="text-stone-500 mb-12 leading-relaxed max-w-md">{product.description}</p>

                        <div className="space-y-8 mb-12">
                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">SIZE</p>
                                <div className="flex space-x-3">
                                    {sizes.map(size => (
                                        <button
                                            key={size}
                                            onClick={() => setSelectedSize(size)}
                                            className={`w-12 h-12 rounded-full text-xs font-bold border transition-all ${selectedSize === size
                                                ? 'bg-black text-white border-black'
                                                : 'border-stone-200 text-stone-600 hover:border-black'
                                                }`}
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">QUANTITY</p>
                                <div className="flex items-center space-x-4 border border-stone-200 w-fit rounded-full px-4 py-2 bg-white">
                                    <button
                                        onClick={() => setQuantity(prev => Math.max(1, prev - 1))}
                                        className="text-stone-400 hover:text-black transition-colors"
                                    >
                                        <Minus size={16} />
                                    </button>
                                    <span className="text-sm font-bold w-6 text-center">{quantity}</span>
                                    <button
                                        onClick={() => setQuantity(prev => prev + 1)}
                                        className="text-stone-400 hover:text-black transition-colors"
                                    >
                                        <Plus size={16} />
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="flex space-x-4">
                            <button className="flex-1 bg-stone-100 py-5 rounded-2xl font-bold hover:bg-stone-200 transition-colors text-stone-900">
                                ADD TO CART
                            </button>
                            <button
                                onClick={handleBuyClick}
                                className="flex-1 bg-black text-white py-5 rounded-2xl font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10"
                            >
                                BUY NOW
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {user && (
                <OrderModal
                    isOpen={isOrderModalOpen}
                    onClose={() => setIsOrderModalOpen(false)}
                    product={product}
                    quantity={quantity}
                    user={user}
                    onOrderSuccess={() => {
                        alert('결제가 완료되었습니다. 주문 상세 페이지로 이동합니다.');
                        onBack();
                        // In a real app, maybe navigate to order history
                    }}
                />
            )}

            <div className="max-w-7xl mx-auto px-6 mt-20 border-t border-stone-200 pt-16">
                <h3 className="text-2xl font-bold mb-8 text-left">Reviews ({product.reviews})</h3>
                <div className="space-y-8">
                    {/* Mock Reviews - Deterministic based on ID */}
                    {Array.from({ length: 3 }).map((_, i) => (
                        <div key={i} className="bg-white p-8 rounded-3xl shadow-sm border border-stone-100 text-left">
                            <div className="flex items-center space-x-2 mb-4">
                                <span className="font-bold">User {1000 + product.id + i}</span>
                                <span className="text-xs text-stone-400">2024.12.{10 + i}</span>
                            </div>
                            <div className="flex text-yellow-400 mb-4">
                                {'★'.repeat(5)}
                            </div>
                            <p className="text-stone-600">
                                This product is amazing! The quality exceeded my expectations.
                                Highly recommended for anyone looking for {product.category.toLowerCase()} items.
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

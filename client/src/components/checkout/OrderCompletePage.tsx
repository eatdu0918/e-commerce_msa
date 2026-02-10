import { CheckCircle, ShoppingBag, ChevronRight } from 'lucide-react';

interface OrderCompletePageProps {
    orderId: number;
    onGoHome: () => void;
    onViewOrder: (orderId: number) => void;
}

export default function OrderCompletePage({ orderId, onGoHome, onViewOrder }: OrderCompletePageProps) {
    return (
        <div className="min-h-screen pt-12 pb-24 bg-[#f9f7f2]">
            <div className="max-w-lg mx-auto px-6 text-center">
                {/* Success Animation */}
                <div className="relative inline-flex items-center justify-center mb-8">
                    <div className="absolute w-32 h-32 bg-green-100 rounded-full animate-ping opacity-20" />
                    <div className="relative w-24 h-24 bg-green-50 rounded-full flex items-center justify-center border-2 border-green-200">
                        <CheckCircle size={48} className="text-green-500" />
                    </div>
                </div>

                <h2 className="text-3xl font-bold tracking-tight mb-3 text-stone-900">주문이 완료되었습니다!</h2>
                <p className="text-stone-400 text-sm mb-10">주문이 성공적으로 접수되었습니다. 감사합니다.</p>

                {/* Order Summary Card */}
                <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100 text-left mb-8">
                    <div className="flex items-center space-x-3 mb-6">
                        <div className="p-2 bg-stone-50 rounded-xl">
                            <ShoppingBag size={20} className="text-stone-500" />
                        </div>
                        <div>
                            <p className="text-xs font-bold text-stone-400 uppercase tracking-widest">주문번호</p>
                            <p className="font-bold">#{orderId}</p>
                        </div>
                    </div>

                    <div className="space-y-3 text-sm border-t border-stone-100 pt-6">
                        <div className="flex justify-between text-stone-400">
                            <span>주문 상태</span>
                            <span className="text-blue-600 font-bold">주문 접수</span>
                        </div>
                        <div className="flex justify-between text-stone-400">
                            <span>예상 배송</span>
                            <span className="text-stone-700">3~5 영업일</span>
                        </div>
                    </div>
                </div>

                {/* Actions */}
                <div className="space-y-3">
                    <button
                        onClick={() => onViewOrder(orderId)}
                        className="w-full bg-black text-white py-4 rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all active:scale-[0.98] flex items-center justify-center space-x-2"
                    >
                        <span>주문 상세보기</span>
                        <ChevronRight size={16} />
                    </button>
                    <button
                        onClick={onGoHome}
                        className="w-full bg-white border border-stone-200 text-stone-700 py-4 rounded-2xl text-sm font-bold hover:bg-stone-50 transition-all"
                    >
                        쇼핑 계속하기
                    </button>
                </div>
            </div>
        </div>
    );
}

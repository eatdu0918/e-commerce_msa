import { useSearchParams, useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';

export default function PaymentFailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const code = searchParams.get('code') || 'UNKNOWN';
    const message = searchParams.get('message') || '결제에 실패했습니다.';

    // 결제 실패 시 pending 데이터 정리
    localStorage.removeItem('pendingOrderData');

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2]">
            <div className="bg-white p-10 rounded-3xl shadow-sm border border-stone-100 max-w-md w-full text-center">
                <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6">
                    <span className="text-3xl">❌</span>
                </div>
                <h2 className="text-xl font-bold mb-3">결제에 실패했습니다</h2>
                <p className="text-sm text-stone-500 mb-2">{message}</p>
                <p className="text-xs text-stone-400 mb-8">오류 코드: {code}</p>
                <div className="space-y-3">
                    <button
                        onClick={() => navigate(-1)}
                        className="w-full bg-black text-white py-3.5 rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all flex items-center justify-center gap-2"
                    >
                        <ChevronLeft size={16} />
                        돌아가서 다시 시도
                    </button>
                    <button
                        onClick={() => navigate('/shop', { replace: true })}
                        className="w-full bg-stone-100 text-stone-600 py-3.5 rounded-2xl text-sm font-medium hover:bg-stone-200 transition-all"
                    >
                        쇼핑으로 돌아가기
                    </button>
                </div>
            </div>
        </div>
    );
}

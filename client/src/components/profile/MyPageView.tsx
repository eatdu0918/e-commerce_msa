import { useTranslation } from 'react-i18next';
import { User, Package, Heart, CreditCard, RotateCcw, Ticket, ChevronRight, MessageSquare, Lock } from 'lucide-react';
import type { UserResponse } from '../../api/services/user';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';

export default function MyPageView() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const user = queryClient.getQueryData<UserResponse>(['user']);

    if (!user) {
        return <div className="p-8 text-center">Please log in to view your profile.</div>;
    }

    const menuItems = [
        { id: 'orders', label: t('common.order_tracking'), icon: <Package size={24} className="text-blue-500" />, path: '/me/orders', desc: 'Check shipping status' },
        { id: 'wishlist', label: t('common.wishlist'), icon: <Heart size={24} className="text-red-500" />, path: '/me/wishlist', desc: 'Your saved items' },
        { id: 'reviews', label: t('common.my_reviews'), icon: <MessageSquare size={24} className="text-green-500" />, path: '/me/reviews', desc: 'Manage your reviews' },
        { id: 'coupons', label: t('common.my_coupons'), icon: <Ticket size={24} className="text-purple-500" />, path: '/me/coupons', desc: 'Available coupons' },
        { id: 'payments', label: t('common.payment_history'), icon: <CreditCard size={24} className="text-indigo-500" />, path: '/me/payments', desc: 'Transaction history' },
        { id: 'cancel-refund', label: t('common.cancel_refund'), icon: <RotateCcw size={24} className="text-orange-500" />, path: '/me/cancel-refund', desc: 'Returns and refunds' },
    ];

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 space-y-10">
            {/* Profile Summary Card */}
            <div className="bg-stone-50 rounded-[30px] p-8 flex flex-col md:flex-row items-center md:items-start space-y-6 md:space-y-0 md:space-x-8 border border-stone-100">
                <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center text-stone-300 border border-stone-200 shadow-inner">
                    <User size={48} strokeWidth={1} />
                </div>
                <div className="flex-1 text-center md:text-left">
                    <div className="flex flex-col md:flex-row md:items-center md:space-x-3 mb-2">
                        <h3 className="text-2xl font-bold">{user.name}</h3>
                        <span className="inline-block px-3 py-1 bg-black text-white text-[10px] font-bold rounded-full w-fit mx-auto md:mx-0">
                            {user.role}
                        </span>
                    </div>
                    <p className="text-stone-400 text-sm mb-6">{user.email}</p>
                    <div className="flex justify-center md:justify-start space-x-12 border-t border-stone-200 pt-6">
                        <div className="text-center group cursor-pointer" onClick={() => navigate('/me/coupons')}>
                            <p className="text-[10px] font-bold text-stone-400 mb-1">COUPON</p>
                            <p className="font-bold text-lg group-hover:text-red-600 transition-colors">0</p>
                        </div>
                        <div className="text-center group cursor-pointer">
                            <p className="text-[10px] font-bold text-stone-400 mb-1">POINTS</p>
                            <p className="font-bold text-lg group-hover:text-blue-600 transition-colors">0</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Quick Menu Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {menuItems.map((item) => (
                    <div
                        key={item.id}
                        onClick={() => navigate(item.path)}
                        className="bg-white p-6 rounded-2xl shadow-sm border border-stone-100 hover:shadow-md transition-shadow cursor-pointer group flex items-start space-x-5"
                    >
                        <div className="p-3 bg-stone-50 rounded-2xl group-hover:scale-110 transition-transform">
                            {item.icon}
                        </div>
                        <div className="flex-1">
                            <h4 className="font-bold mb-1 flex items-center">
                                {item.label}
                                <ChevronRight size={16} className="ml-1 text-stone-300 group-hover:translate-x-1 transition-transform" />
                            </h4>
                            <p className="text-xs text-stone-400">{item.desc}</p>
                        </div>
                    </div>
                ))}
            </div>

            {/* Account Settings */}
            <div className="pt-6">
                <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-6 px-2">{t('common.my_info')}</h4>
                <div className="bg-white rounded-3xl border border-stone-100 overflow-hidden divide-y divide-stone-50">
                    <button
                        onClick={() => navigate('/me/profile')}
                        className="w-full px-6 py-5 text-left text-sm hover:bg-stone-50 transition-colors flex justify-between items-center group"
                    >
                        <span className="font-medium">{t('common.edit_profile')}</span>
                        <ChevronRight size={16} className="text-stone-300 group-hover:translate-x-1 transition-transform" />
                    </button>
                    <button
                        onClick={() => navigate('/me/password')}
                        className="w-full px-6 py-5 text-left text-sm hover:bg-stone-50 transition-colors flex justify-between items-center group"
                    >
                        <span className="font-medium flex items-center gap-2">
                            <Lock size={15} className="text-stone-400" />
                            비밀번호 변경
                        </span>
                        <ChevronRight size={16} className="text-stone-300 group-hover:translate-x-1 transition-transform" />
                    </button>
                </div>
            </div>
        </div>
    );
}

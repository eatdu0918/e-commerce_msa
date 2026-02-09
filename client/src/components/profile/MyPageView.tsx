import { useTranslation } from 'react-i18next';
import { ShoppingBag, Heart, Gift, MessageSquare, ChevronRight, User } from 'lucide-react';
import type { UserResponse } from '../../api/services/user';

interface MyPageViewProps {
    user: UserResponse;
    onNavigate: (view: string) => void;
}

export default function MyPageView({ user, onNavigate }: MyPageViewProps) {
    const { t } = useTranslation();

    const menuItems = [
        {
            title: t('common.order_tracking'),
            icon: <ShoppingBag className="text-blue-500" size={24} />,
            view: 'order',
            desc: '주문하신 상품의 배송 상태를 확인하세요.'
        },
        {
            title: t('common.wishlist') || '찜 목록',
            icon: <Heart className="text-red-500" size={24} />,
            view: 'wishlist',
            desc: '나중에 구매하려고 찜해둔 상품들입니다.'
        },
        {
            title: t('benefit.title') || '나의 혜택',
            icon: <Gift className="text-purple-500" size={24} />,
            view: 'benefit',
            desc: '쿠폰 및 적립급 혜택을 확인하세요.'
        },
        {
            title: t('activity.title') || '리뷰 관리',
            icon: <MessageSquare className="text-green-500" size={24} />,
            view: 'activity',
            desc: '내가 작성한 소중한 리뷰들을 관리하세요.'
        }
    ];

    return (
        <div className="space-y-10">
            {/* Profile Summary Card */}
            <div className="bg-stone-50 rounded-[30px] p-8 flex flex-col md:flex-row items-center md:items-start space-y-6 md:space-y-0 md:space-x-8 border border-stone-100">
                <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center text-stone-300 border border-stone-200 shadow-inner">
                    <User size={48} strokeWidth={1} />
                </div>
                <div className="flex-1 text-center md:text-left">
                    <div className="flex flex-col md:flex-row md:items-center md:space-x-3 mb-2">
                        <h3 className="text-2xl font-bold">{user.username} 님</h3>
                        <span className="inline-block px-3 py-1 bg-black text-white text-[10px] font-bold rounded-full w-fit mx-auto md:mx-0">
                            {user.role}
                        </span>
                    </div>
                    <p className="text-stone-400 text-sm mb-6">{user.email}</p>
                    <div className="flex justify-center md:justify-start space-x-12 border-t border-stone-200 pt-6">
                        <div className="text-center group cursor-pointer" onClick={() => onNavigate('benefit')}>
                            <p className="text-[10px] font-bold text-stone-400 mb-1">CASH</p>
                            <p className="font-bold text-lg group-hover:text-blue-600 transition-colors">1,200</p>
                        </div>
                        <div className="text-center group cursor-pointer" onClick={() => onNavigate('benefit')}>
                            <p className="text-[10px] font-bold text-stone-400 mb-1">COUPON</p>
                            <p className="font-bold text-lg group-hover:text-red-600 transition-colors">2</p>
                        </div>
                        <div className="text-center group cursor-pointer" onClick={() => onNavigate('activity')}>
                            <p className="text-[10px] font-bold text-stone-400 mb-1">REVIEW</p>
                            <p className="font-bold text-lg group-hover:text-green-600 transition-colors">1</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Quick Menu Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {menuItems.map((item) => (
                    <button
                        key={item.view}
                        onClick={() => onNavigate(item.view)}
                        className="group bg-white p-6 rounded-3xl border border-stone-100 hover:border-stone-300 hover:shadow-md transition-all text-left flex items-start space-x-5"
                    >
                        <div className="p-3 bg-stone-50 rounded-2xl group-hover:scale-110 transition-transform">
                            {item.icon}
                        </div>
                        <div className="flex-1">
                            <h4 className="font-bold mb-1 flex items-center">
                                {item.title}
                                <ChevronRight size={16} className="ml-1 text-stone-300 group-hover:translate-x-1 transition-transform" />
                            </h4>
                            <p className="text-xs text-stone-400">{item.desc}</p>
                        </div>
                    </button>
                ))}
            </div>

            {/* Account Settings */}
            <div className="pt-6">
                <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-6 px-2">{t('common.my_info')}</h4>
                <div className="bg-white rounded-3xl border border-stone-100 overflow-hidden divide-y divide-stone-50">
                    <button
                        onClick={() => onNavigate('edit_profile')}
                        className="w-full px-6 py-5 text-left text-sm hover:bg-stone-50 transition-colors flex justify-between items-center group"
                    >
                        <span className="font-medium">{t('common.edit_profile')}</span>
                        <ChevronRight size={16} className="text-stone-300 group-hover:translate-x-1 transition-transform" />
                    </button>
                    <button className="w-full px-6 py-5 text-left text-sm text-stone-300 cursor-not-allowed flex justify-between items-center">
                        <span className="font-medium">알림 설정</span>
                        <span className="text-[10px] font-bold uppercase tracking-tighter bg-stone-50 px-2 py-0.5 rounded text-stone-300">Coming soon</span>
                    </button>
                </div>
            </div>
        </div>
    );
}

import { Search, ShoppingBag, Menu, X, User, Bell } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';

interface HeaderProps {
    category: string;
    setCategory: (category: string) => void;
    categories: string[];
    onNavigate: (view: string) => void;
    view?: string;
}

export default function Header({ category, setCategory, categories, onNavigate, view }: HeaderProps) {

    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [userDropdownOpen, setUserDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setUserDropdownOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <nav className="sticky top-0 z-[100] bg-white/80 backdrop-blur-md border-b border-stone-200">
            <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
                <div
                    className="text-xl font-bold tracking-tighter cursor-pointer"
                    onClick={() => {
                        setCategory('전체');
                        onNavigate('home');
                    }}
                >
                    URBAN THREADS
                </div>

                <div className="hidden md:flex space-x-8 text-sm font-medium text-stone-600">
                    <button
                        onClick={() => onNavigate('all_products')}
                        className={`hover:text-black transition-colors ${view === 'all_products' ? 'text-black font-bold' : ''}`}
                    >
                        SHOP ALL
                    </button>
                    {categories.map((cat) => (
                        <button
                            key={cat}
                            onClick={() => {
                                setCategory(cat);
                                onNavigate('home');
                            }}
                            className={`hover:text-black transition-colors ${category === cat ? 'text-black' : ''
                                }`}
                        >
                            {cat}
                        </button>
                    ))}
                </div>


                <div className="flex items-center space-x-1 md:space-x-4">
                    <button className="p-2 hover:bg-stone-100 rounded-full transition-colors">
                        <Search size={20} strokeWidth={2} />
                    </button>

                    {/* User Dropdown */}
                    <div className="relative" ref={dropdownRef}>
                        <button
                            className="p-2 hover:bg-stone-100 rounded-full transition-colors flex items-center"
                            onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                        >
                            <User size={20} strokeWidth={2} />
                        </button>

                        {userDropdownOpen && (
                            <div className="absolute right-0 mt-3 w-80 bg-white border border-stone-200 rounded-2xl shadow-xl overflow-hidden z-[110]">
                                <div className="p-6 bg-stone-50 border-b border-stone-100">
                                    <div className="flex items-center space-x-3">
                                        <div className="w-12 h-12 bg-stone-200 rounded-full flex items-center justify-center text-stone-500 font-bold">JD</div>
                                        <div className="text-left">
                                            <p className="font-bold text-sm">홍길동 님</p>
                                            <p className="text-xs text-stone-400">platinum 멤버십</p>
                                        </div>
                                    </div>
                                </div>
                                <div className="max-h-[400px] overflow-y-auto no-scrollbar">
                                    <div className="p-4 border-b border-stone-50">
                                        <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3 text-left">MY 쇼핑</h4>
                                        <ul className="space-y-3 text-sm text-left">
                                            <li
                                                className="flex justify-between items-center cursor-pointer hover:text-blue-600 transition-colors"
                                                onClick={() => {
                                                    onNavigate('order');
                                                    setUserDropdownOpen(false);
                                                }}
                                            >
                                                <span>주문목록/배송조회</span>
                                                <span className="text-xs bg-stone-100 px-2 py-0.5 rounded text-stone-500 font-medium">3건</span>
                                            </li>
                                            <li className="cursor-pointer hover:text-black transition-colors">취소/반품/환불 내역</li>
                                            <li className="flex items-center justify-between cursor-pointer hover:text-black transition-colors">
                                                <span>와우 멤버십</span>
                                                <span className="w-2 h-2 bg-red-500 rounded-full"></span>
                                            </li>
                                        </ul>
                                    </div>
                                    <div className="p-4 border-b border-stone-50">
                                        <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3 text-left">MY 혜택</h4>
                                        <ul className="space-y-3 text-sm text-left">
                                            <li
                                                className="flex justify-between items-center cursor-pointer hover:text-blue-600 transition-colors"
                                                onClick={() => {
                                                    onNavigate('benefit');
                                                    setUserDropdownOpen(false);
                                                }}
                                            >
                                                <span>쿠폰 · 이용권</span>
                                                <span className="text-xs font-bold text-red-500">2장</span>
                                            </li>
                                            <li className="flex justify-between items-center cursor-pointer hover:text-black transition-colors">
                                                <span>쿠팡캐시/기프트카드</span>
                                                <span className="text-xs">1,200원</span>
                                            </li>
                                        </ul>
                                    </div>
                                    <div className="p-4 border-b border-stone-50">
                                        <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3 text-left">MY 활동</h4>
                                        <ul className="space-y-3 text-sm text-left">
                                            <li className="cursor-pointer hover:text-black transition-colors">문의하기</li>
                                            <li
                                                className="flex justify-between items-center cursor-pointer hover:text-blue-600 transition-colors"
                                                onClick={() => {
                                                    onNavigate('activity');
                                                    setUserDropdownOpen(false);
                                                }}
                                            >
                                                <span>리뷰관리</span>
                                                <span className="text-[10px] bg-red-100 text-red-600 px-1.5 py-0.5 rounded font-bold">NEW</span>
                                            </li>
                                        </ul>
                                    </div>
                                    <div className="p-4">
                                        <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3 text-left">MY 정보</h4>
                                        <ul className="space-y-3 text-sm text-left">
                                            <li className="cursor-pointer hover:text-black transition-colors">개인정보확인/수정</li>
                                            <li className="cursor-pointer hover:text-black transition-colors">배송지 관리</li>
                                            <li className="text-stone-300 cursor-pointer hover:text-red-400 transition-colors text-xs pt-2">회원 탈퇴</li>
                                        </ul>
                                    </div>
                                </div>
                                <button className="w-full py-4 bg-stone-900 text-white text-xs font-bold hover:bg-black transition-colors">
                                    로그아웃
                                </button>
                            </div>
                        )}
                    </div>

                    <button className="p-2 hover:bg-stone-100 rounded-full transition-colors relative">
                        <Bell size={20} strokeWidth={2} />
                        <span className="absolute top-2 right-2 w-1.5 h-1.5 bg-red-500 rounded-full"></span>
                    </button>

                    <div className="relative">
                        <button className="p-2 hover:bg-stone-100 rounded-full transition-colors relative">
                            <ShoppingBag size={20} strokeWidth={2} />
                            <span className="absolute top-1 right-1 bg-red-500 text-white text-[10px] w-4 h-4 flex items-center justify-center rounded-full font-bold">
                                2
                            </span>
                        </button>
                    </div>

                    <button
                        className="md:hidden p-2 hover:bg-stone-100 rounded-full transition-colors"
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    >
                        {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
                    </button>
                </div>
            </div>

            {/* Mobile Menu */}
            {mobileMenuOpen && (
                <div className="md:hidden bg-white border-t border-stone-200 py-4 px-6">
                    <div className="flex flex-col space-y-4 text-sm font-medium text-stone-600 text-left">
                        <button
                            onClick={() => {
                                onNavigate('all_products');
                                setMobileMenuOpen(false);
                            }}
                            className={`hover:text-black transition-colors ${view === 'all_products' ? 'text-black' : ''}`}
                        >
                            SHOP ALL
                        </button>
                        {categories.map((cat) => (
                            <button
                                key={cat}
                                onClick={() => {
                                    setCategory(cat);
                                    setMobileMenuOpen(false);
                                    onNavigate('home');
                                }}
                                className={`text-left hover:text-black transition-colors ${category === cat ? 'text-black' : ''
                                    }`}
                            >
                                {cat}
                            </button>
                        ))}
                    </div>

                </div>
            )}
        </nav>
    );
}

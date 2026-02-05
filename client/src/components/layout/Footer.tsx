export default function Footer() {
    return (
        <footer className="bg-white border-t border-stone-200 pt-20 pb-10">
            <div className="max-w-7xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 gap-12 mb-20">
                <div className="col-span-2 md:col-span-1">
                    <div className="text-xl font-bold tracking-tighter mb-6">URBAN THREADS</div>
                    <p className="text-stone-500 text-sm leading-relaxed">
                        단순함의 미학을 추구합니다. <br />
                        우리는 가장 기본적인 것에 집중하며, <br />
                        오랜 시간 변치 않는 가치를 제안합니다.
                    </p>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">SHOP</h5>
                    <ul className="text-stone-500 text-sm space-y-4">
                        <li><a href="#" className="hover:text-black">All Collections</a></li>
                        <li><a href="#" className="hover:text-black">Winter Sale</a></li>
                        <li><a href="#" className="hover:text-black">New Arrivals</a></li>
                        <li><a href="#" className="hover:text-black">Gift Cards</a></li>
                    </ul>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">SUPPORT</h5>
                    <ul className="text-stone-500 text-sm space-y-4">
                        <li><a href="#" className="hover:text-black">Shipping & Returns</a></li>
                        <li><a href="#" className="hover:text-black">Size Guide</a></li>
                        <li><a href="#" className="hover:text-black">Contact Us</a></li>
                        <li><a href="#" className="hover:text-black">FAQ</a></li>
                    </ul>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">NEWSLETTER</h5>
                    <p className="text-stone-500 text-xs mb-4">최신 소식과 혜택을 이메일로 받아보세요.</p>
                    <div className="flex">
                        <input
                            type="email"
                            placeholder="이메일 주소"
                            className="bg-stone-50 border border-stone-200 px-4 py-2 text-sm w-full focus:outline-none focus:border-black rounded-l-md"
                        />
                        <button className="bg-black text-white px-4 py-2 text-xs font-bold rounded-r-md">가입</button>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-6 border-t border-stone-100 pt-8 flex flex-col md:flex-row items-center justify-between space-y-4 md:space-y-0 text-stone-400 text-[10px]">
                <p>&copy; 2024 URBAN THREADS. All Rights Reserved.</p>
                <div className="flex space-x-6">
                    <a href="#" className="hover:text-black">개인정보처리방침</a>
                    <a href="#" className="hover:text-black">이용약관</a>
                </div>
            </div>
        </footer>
    );
}

import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Hero() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <section className="pt-32 pb-20 px-6">
            <div className="max-w-7xl mx-auto">
                <div className="rounded-[40px] bg-[#EAEAEA] min-h-[600px] relative overflow-hidden flex items-center px-10 md:px-20">
                    <div className="relative z-10 max-w-xl text-left">
                        <span className="inline-block px-4 py-2 bg-white rounded-full text-xs font-bold mb-6 tracking-wide text-stone-600">
                            NEW COLLECTION 2024
                        </span>
                        <h1 className="text-6xl md:text-8xl font-bold tracking-tighter mb-8 text-stone-900 leading-[0.9]">
                            The Future of <br />
                            <span className="text-stone-500">Shopping</span>
                        </h1>
                        <p className="text-lg text-stone-600 mb-10 max-w-md leading-relaxed font-light">
                            Experience a curated collection of premium essentials designed for modern living.
                            Quality meets minimalist aesthetics.
                        </p>
                        <button
                            onClick={() => navigate('/shop')}
                            className="bg-stone-900 text-white px-10 py-5 rounded-full font-bold text-lg hover:bg-black transition-all hover:scale-105 shadow-xl shadow-stone-200"
                        >
                            {t('common.shop_now')}
                        </button>
                    </div>
                    <div className="absolute right-0 top-0 bottom-0 w-1/2 bg-[url('https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&q=80')] bg-cover bg-center opacity-0 md:opacity-100 mask-image-gradient">
                        <div className="absolute inset-0 bg-gradient-to-l from-transparent to-[#EAEAEA]"></div>
                    </div>
                </div>
            </div>
        </section>
    );
}

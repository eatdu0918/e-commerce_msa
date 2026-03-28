import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function CategoryGrid() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const categories = [
        {
            titleKey: 'category.knitwear_title',
            subtitleKey: 'category.knitwear_sub',
            image: '/assets/images/category_knitwear.png',
            id: 'fashion'
        },
        {
            titleKey: 'category.denim_title',
            subtitleKey: 'category.denim_sub',
            image: '/assets/images/category_denim.png',
            id: 'fashion'
        },
        {
            titleKey: 'category.decor_title',
            subtitleKey: 'category.decor_sub',
            image: '/assets/images/category_decor.png',
            id: 'home'
        },
        {
            titleKey: 'category.lifestyle_title',
            subtitleKey: 'category.lifestyle_sub',
            image: '/assets/images/category_lifestyle.png',
            id: 'fashion'
        },
    ];

    return (
        <section className="max-w-7xl mx-auto px-6 py-20">
            <h2 className="text-2xl font-bold tracking-tight mb-10 text-left">{t('category.shop_by')}</h2>
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
                {categories.map((cat, index) => (
                    <div
                        key={index}
                        className="group cursor-pointer"
                        onClick={() => navigate(`/shop?category=${cat.id}`)}
                    >
                        <div className="aspect-square bg-[#ece8e2] rounded-2xl p-8 mb-4 transition-transform duration-500 group-hover:scale-[1.02]">
                            <img
                                src={cat.image}
                                alt={t(cat.titleKey)}
                                className="w-full h-full object-contain mix-blend-multiply opacity-80"
                            />
                        </div>
                        <h3 className="font-medium text-center">{t(cat.titleKey)}</h3>
                        <p className="text-stone-400 text-xs text-center mt-1">{t(cat.subtitleKey)}</p>
                    </div>
                ))}
            </div>
        </section>
    );
}

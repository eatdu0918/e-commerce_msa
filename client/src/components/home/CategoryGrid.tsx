import { useNavigate } from 'react-router-dom';

export default function CategoryGrid() {
    const navigate = useNavigate();
    const categories = [
        {
            title: 'Cozy Knitwear',
            subtitle: 'Soften up your look',
            image: '/assets/images/category_knitwear.png',
            id: 'fashion'
        },
        {
            title: 'Essential Denim',
            subtitle: 'Everyday essentials',
            image: '/assets/images/category_denim.png',
            id: 'fashion'
        },
        {
            title: 'Curated Decor',
            subtitle: 'Interior inspirations',
            image: '/assets/images/category_decor.png',
            id: 'home'
        },
        {
            title: 'Active Lifestyle',
            subtitle: 'Keep moving',
            image: '/assets/images/category_lifestyle.png',
            id: 'fashion'
        },
    ];

    return (
        <section className="max-w-7xl mx-auto px-6 py-20">
            <h2 className="text-2xl font-bold tracking-tight mb-10 text-left">SHOP BY CATEGORY</h2>
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
                                alt={cat.title}
                                className="w-full h-full object-contain mix-blend-multiply opacity-80"
                            />
                        </div>
                        <h3 className="font-medium text-center">{cat.title}</h3>
                        <p className="text-stone-400 text-xs text-center mt-1">{cat.subtitle}</p>
                    </div>
                ))}
            </div>
        </section>
    );
}

interface HeroProps {
    onShopClick?: () => void;
}

export default function Hero({ onShopClick }: HeroProps) {

    return (
        <header className="relative overflow-hidden min-h-[600px] flex items-center">
            {/* Background shape from demo.html */}
            <div className="hero-bg-shape"></div>

            <div className="max-w-7xl mx-auto px-6 grid md:grid-cols-2 gap-12 items-center w-full text-left">
                <div className="z-10">
                    <h1 className="text-5xl md:text-7xl font-light tracking-tight leading-tight mb-6">
                        Effortless Style<br /> for the <br />
                        <span className="font-medium text-stone-800">Modern Minimalist</span>
                    </h1>
                    <p className="text-stone-500 text-lg mb-8 max-w-md">
                        Your daily life completed with understated sophistication and highest quality.
                    </p>
                    <button
                        onClick={onShopClick}
                        className="bg-white border border-stone-300 px-8 py-3 rounded-full text-sm font-medium hover:bg-black hover:text-white hover:border-black transition-all duration-300"
                    >
                        SHOP THE COLLECTION
                    </button>

                </div>

                <div className="relative h-[500px] hidden md:block text-left">
                    <img
                        src="/assets/images/hero_model.png"
                        alt="Hero"
                        className="absolute right-0 top-1/2 -translate-y-1/2 w-[90%] h-full object-cover rounded-3xl shadow-2xl shadow-stone-300/50"
                    />
                </div>

            </div>
        </header>
    );
}


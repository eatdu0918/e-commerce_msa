import { useTranslation } from 'react-i18next';

export default function Footer() {
    const { t } = useTranslation();
    return (
        <footer className="bg-white border-t border-stone-200 pt-20 pb-10">
            <div className="max-w-7xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 gap-12 mb-20">
                <div className="col-span-2 md:col-span-1">
                    <div className="text-xl font-bold tracking-tighter mb-6">URBAN THREADS</div>
                    <p className="text-stone-500 text-sm leading-relaxed">
                        {t('footer.brand_desc_1')} <br />
                        {t('footer.brand_desc_2')} <br />
                        {t('footer.brand_desc_3')}
                    </p>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">{t('footer.shop')}</h5>
                    <ul className="text-stone-500 text-sm space-y-4">
                        <li><a href="#" className="hover:text-black">{t('footer.all_collections')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.winter_sale')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.new_arrivals')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.gift_cards')}</a></li>
                    </ul>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">{t('footer.support')}</h5>
                    <ul className="text-stone-500 text-sm space-y-4">
                        <li><a href="#" className="hover:text-black">{t('footer.shipping_returns')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.size_guide')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.contact_us')}</a></li>
                        <li><a href="#" className="hover:text-black">{t('footer.faq')}</a></li>
                    </ul>
                </div>
                <div>
                    <h5 className="font-bold text-sm mb-6">{t('footer.newsletter')}</h5>
                    <p className="text-stone-500 text-xs mb-4">{t('footer.newsletter_desc')}</p>
                    <div className="flex">
                        <input
                            type="email"
                            placeholder={t('footer.email_placeholder')}
                            className="bg-stone-50 border border-stone-200 px-4 py-2 text-sm w-full focus:outline-none focus:border-black rounded-l-md"
                        />
                        <button className="bg-black text-white px-4 py-2 text-xs font-bold rounded-r-md">{t('footer.subscribe')}</button>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-6 border-t border-stone-100 pt-8 flex flex-col md:flex-row items-center justify-between space-y-4 md:space-y-0 text-stone-400 text-[10px]">
                <p>&copy; 2024 URBAN THREADS. All Rights Reserved.</p>
                <div className="flex space-x-6">
                    <a href="#" className="hover:text-black">{t('footer.privacy')}</a>
                    <a href="#" className="hover:text-black">{t('footer.terms')}</a>
                </div>
            </div>
        </footer>
    );
}

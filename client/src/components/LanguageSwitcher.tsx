import { useTranslation } from 'react-i18next';

export default function LanguageSwitcher() {
    const { i18n } = useTranslation();

    const changeLanguage = (lng: string) => {
        i18n.changeLanguage(lng);
    };

    return (
        <div className="flex items-center space-x-2 text-sm font-medium">
            <button
                onClick={() => changeLanguage('ko')}
                className={`transition-colors ${i18n.language === 'ko' ? 'text-black font-bold' : 'text-stone-400 hover:text-stone-600'}`}
            >
                KO
            </button>
            <span className="text-stone-300">|</span>
            <button
                onClick={() => changeLanguage('en')}
                className={`transition-colors ${i18n.language.startsWith('en') ? 'text-black font-bold' : 'text-stone-400 hover:text-stone-600'}`}
            >
                EN
            </button>
        </div>
    );
}

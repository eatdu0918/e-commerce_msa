import { useNavigate } from 'react-router-dom';
import { Home, AlertTriangle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';

export default function NotFoundPage() {
    const navigate = useNavigate();
    const { t } = useTranslation();

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2] px-6">
            <Helmet>
                <title>Page Not Found | Urban Threads</title>
            </Helmet>

            <div className="max-w-md w-full text-center">
                <div className="mb-8 flex justify-center">
                    <div className="w-24 h-24 bg-stone-100 rounded-full flex items-center justify-center text-stone-300">
                        <AlertTriangle size={48} strokeWidth={1.5} />
                    </div>
                </div>

                <h1 className="text-4xl font-bold mb-4 text-stone-900">
                    {t('error.not_found_title') || 'Page Not Found'}
                </h1>

                <p className="text-stone-500 mb-10 leading-relaxed">
                    {t('error.not_found_desc') || "The page you are looking for doesn't exist or has been moved."}
                </p>

                <button
                    onClick={() => navigate('/')}
                    className="inline-flex items-center justify-center px-8 py-4 bg-black text-white rounded-2xl font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10 group"
                >
                    <Home size={18} className="mr-2 group-hover:-translate-y-0.5 transition-transform" />
                    <span>{t('common.go_home') || 'Go Back Home'}</span>
                </button>
            </div>
        </div>
    );
}

import { login, getMyProfile } from '../../api/services/user';
import { X, Lock, Mail } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useState } from 'react';
import { useScrollLock } from '../../hooks/useScrollLock';
import { useTranslation } from 'react-i18next';
import { AUTH_STORAGE_KEYS } from '../../lib/authStorage';

interface LoginModalProps {
    isOpen: boolean;
    onClose: () => void;
    onLoginSuccess: () => void;
    onSignupClick: () => void;
}

export default function LoginModal({ isOpen, onClose, onLoginSuccess, onSignupClick }: LoginModalProps) {
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);

    const loginSchema = z.object({
        email: z.string().email(t('auth.email_required')),
        password: z.string().min(1, t('auth.password_required')),
    });

    type LoginFormValues = z.infer<typeof loginSchema>;

    const { register, handleSubmit, formState: { errors }, setError } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema)
    });

    useScrollLock(isOpen);

    if (!isOpen) return null;

    const onSubmit = async (data: LoginFormValues) => {
        setLoading(true);
        try {
            const response = await login({ email: data.email, password: data.password });
            localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, response.accessToken);
            localStorage.setItem(AUTH_STORAGE_KEYS.refreshToken, response.refreshToken);

            const userProfile = await getMyProfile();
            localStorage.setItem(AUTH_STORAGE_KEYS.user, JSON.stringify(userProfile));
            localStorage.setItem(AUTH_STORAGE_KEYS.role, userProfile.role);

            onLoginSuccess();
            onClose();
        } catch (_err: any) {
            setError('root', { message: t('auth.login_failed') });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm cursor-pointer"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl relative cursor-default"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    className="absolute top-6 right-6 text-stone-400 hover:text-black transition-colors"
                >
                    <X size={24} />
                </button>

                <h2 className="text-3xl font-bold mb-2">{t('auth.welcome_back')}</h2>
                <p className="text-stone-500 mb-8">{t('auth.welcome_to')}</p>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                    <div className="relative">
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('auth.email')}</label>
                        <div className="relative">
                            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                            <input
                                type="email"
                                {...register('email')}
                                className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all ${errors.email ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                    }`}
                                placeholder={t('auth.email_placeholder')}
                            />
                        </div>
                        {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('auth.password')}</label>
                        <div className="relative">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                            <input
                                type="password"
                                {...register('password')}
                                className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all ${errors.password ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                    }`}
                                placeholder={t('auth.password_placeholder')}
                            />
                        </div>
                        {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>}
                    </div>

                    {errors.root && <p className="text-red-500 text-sm font-medium bg-red-50 py-2 px-3 rounded-lg text-center">{errors.root.message}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? t('auth.processing') : t('auth.login')}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-stone-500">
                    {t('auth.no_account')} <button onClick={onSignupClick} className="font-bold text-black underline">{t('auth.signup')}</button>
                </div>
            </div>
        </div>
    );
}

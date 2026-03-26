import { useState } from 'react';
import { Mail, Lock, User, Phone, MapPin, ArrowLeft } from 'lucide-react';
import { signup } from '../../api/services/user';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import toast from 'react-hot-toast';
import { Helmet } from 'react-helmet-async';
import { useTranslation } from 'react-i18next';

export default function SignupPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const signupSchema = z.object({
        email: z.string().email(t('auth.email_required')),
        password: z.string().min(6, t('review.content_length_error')), // Reuse some keys or add new ones if needed, but sticking to auth keys
        confirmPassword: z.string(),
        name: z.string().min(2, t('auth.name')),
        phoneNumber: z.string().regex(/^\d{2,3}-\d{3,4}-\d{4}$/, t('auth.phone')),
        address: z.string().min(1, t('auth.address')),
        gender: z.enum(['MALE', 'FEMALE']),
    }).refine((data) => data.password === data.confirmPassword, {
        message: t('auth.confirm_password'),
        path: ["confirmPassword"],
    });

    type SignupFormValues = z.infer<typeof signupSchema>;

    const { register, handleSubmit, formState: { errors }, setError } = useForm<SignupFormValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: {
            gender: 'MALE'
        }
    });

    const onSubmit = async (data: SignupFormValues) => {
        setLoading(true);
        try {
            await signup({
                email: data.email,
                password: data.password,
                name: data.name,
                role: 'CUSTOMER',
                address: data.address,
                phoneNumber: data.phoneNumber,
                gender: data.gender
            });
            toast.success(t('auth.signup_success'));
            navigate('/');
        } catch (err: any) {
            if (err.response?.status === 409) {
                setError('email', { message: t('auth.email_exists') });
            } else {
                toast.error(err.response?.data?.message || t('auth.signup_error'));
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-stone-50 flex flex-col justify-center items-center p-6">
            <Helmet>
                <title>{t('auth.signup')} | Sparta Shop</title>
                <meta name="description" content="Create your account to start shopping." />
            </Helmet>
            <div className="w-full max-w-md bg-white rounded-3xl shadow-xl p-8 md:p-12 relative">
                <button
                    onClick={() => navigate(-1)}
                    className="absolute top-8 left-8 text-stone-400 hover:text-black transition-colors"
                >
                    <ArrowLeft size={24} />
                </button>

                <div className="text-center mb-10">
                    <h1 className="text-3xl font-bold tracking-tight mb-2">{t('auth.create_account')}</h1>
                    <p className="text-stone-500 text-sm">{t('auth.join_today')}</p>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                    <div className="relative">
                        <User className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="text"
                            placeholder={t('auth.name')}
                            {...register('name')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.name ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.name && <p className="text-xs text-red-500 mt-1 ml-1">{errors.name.message}</p>}
                    </div>

                    <div className="relative">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="email"
                            placeholder={t('auth.email')}
                            {...register('email')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.email ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.email && <p className="text-xs text-red-500 mt-1 ml-1">{errors.email.message}</p>}
                    </div>

                    <div className="relative">
                        <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="tel"
                            placeholder={t('auth.phone')}
                            {...register('phoneNumber')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.phoneNumber ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.phoneNumber && <p className="text-xs text-red-500 mt-1 ml-1">{errors.phoneNumber.message}</p>}
                    </div>

                    <div className="relative">
                        <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="text"
                            placeholder={t('auth.address')}
                            {...register('address')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.address ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.address && <p className="text-xs text-red-500 mt-1 ml-1">{errors.address.message}</p>}
                    </div>

                    <div className="relative">
                        <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="password"
                            placeholder={t('auth.password')}
                            {...register('password')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.password ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.password && <p className="text-xs text-red-500 mt-1 ml-1">{errors.password.message}</p>}
                    </div>

                    <div className="relative">
                        <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="password"
                            placeholder={t('auth.confirm_password')}
                            {...register('confirmPassword')}
                            className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all bg-stone-50/50 ${errors.confirmPassword ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                }`}
                        />
                        {errors.confirmPassword && <p className="text-xs text-red-500 mt-1 ml-1">{errors.confirmPassword.message}</p>}
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-all hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:hover:scale-100 shadow-xl shadow-stone-200"
                    >
                        {loading ? t('auth.processing') : t('auth.signup')}
                    </button>
                </form>

                <div className="mt-8 text-center">
                    <p className="text-stone-500 text-sm">
                        {t('auth.already_have_account')}{' '}
                        <button onClick={() => navigate(-1)} className="text-black font-bold hover:underline">
                            {t('auth.log_in')}
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}

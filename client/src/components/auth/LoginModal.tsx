import { login, getMyProfile } from '../../api/services/user';
import { X, Lock, Mail } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useState } from 'react';
import { useScrollLock } from '../../hooks/useScrollLock';

interface LoginModalProps {
    isOpen: boolean;
    onClose: () => void;
    onLoginSuccess: () => void;
    onSignupClick: () => void;
}

const loginSchema = z.object({
    email: z.string().email('이메일을 입력해주세요.'),
    password: z.string().min(1, '비밀번호를 입력해주세요.'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginModal({ isOpen, onClose, onLoginSuccess, onSignupClick }: LoginModalProps) {
    const [loading, setLoading] = useState(false);
    const { register, handleSubmit, formState: { errors }, setError } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema)
    });

    useScrollLock(isOpen);

    if (!isOpen) return null;

    const onSubmit = async (data: LoginFormValues) => {
        setLoading(true);
        try {
            const response = await login({ email: data.email, password: data.password });
            sessionStorage.setItem('accessToken', response.accessToken);

            const userProfile = await getMyProfile();
            sessionStorage.setItem('user', JSON.stringify(userProfile));

            onLoginSuccess();
            onClose();
        } catch (_err: any) {
            setError('root', { message: '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl relative">
                <button
                    onClick={onClose}
                    className="absolute top-6 right-6 text-stone-400 hover:text-black transition-colors"
                >
                    <X size={24} />
                </button>

                <h2 className="text-3xl font-bold mb-2">Welcome Back</h2>
                <p className="text-stone-500 mb-8">URBAN THREADS에 오신 것을 환영합니다.</p>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                    <div className="relative">
                        <label className="block text-sm font-bold text-stone-700 mb-2">Email</label>
                        <div className="relative">
                            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                            <input
                                type="email"
                                {...register('email')}
                                className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all ${errors.email ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                    }`}
                                placeholder="hello@example.com"
                            />
                        </div>
                        {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">Password</label>
                        <div className="relative">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                            <input
                                type="password"
                                {...register('password')}
                                className={`w-full pl-12 pr-4 py-3 rounded-xl border focus:outline-none focus:ring-2 transition-all ${errors.password ? 'border-red-300 focus:ring-red-200' : 'border-stone-200 focus:ring-black'
                                    }`}
                                placeholder="••••••••"
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
                        {loading ? 'Processing...' : 'Login'}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-stone-500">
                    계정이 없으신가요? <button onClick={onSignupClick} className="font-bold text-black underline">회원가입</button>
                </div>
            </div>
        </div>
    );
}

import { useState } from 'react';
import { Mail, Lock, User, Phone, MapPin, ArrowLeft } from 'lucide-react';
import { signup } from '../../api/services/user';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import toast from 'react-hot-toast';
import { Helmet } from 'react-helmet-async';

const signupSchema = z.object({
    email: z.string().email('올바른 이메일 형식이 아닙니다.'),
    password: z.string().min(6, '비밀번호는 최소 6자 이상이어야 합니다.'),
    confirmPassword: z.string(),
    name: z.string().min(2, '이름을 2자 이상 입력해주세요.'),
    phoneNumber: z.string().regex(/^\d{2,3}-\d{3,4}-\d{4}$/, '올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)'),
    address: z.string().min(1, '주소를 입력해주세요.'),
    gender: z.enum(['MALE', 'FEMALE']),
}).refine((data) => data.password === data.confirmPassword, {
    message: "비밀번호가 일치하지 않습니다.",
    path: ["confirmPassword"],
});

type SignupFormValues = z.infer<typeof signupSchema>;

export default function SignupPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

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
            toast.success('회원가입이 완료되었습니다. 로그인해주세요.');
            navigate('/');
        } catch (err: any) {
            if (err.response?.status === 409) {
                setError('email', { message: '이미 존재하는 이메일입니다.' });
            } else {
                toast.error(err.response?.data?.message || '회원가입 중 오류가 발생했습니다.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-stone-50 flex flex-col justify-center items-center p-6">
            <Helmet>
                <title>Sign Up | Sparta Shop</title>
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
                    <h1 className="text-3xl font-bold tracking-tight mb-2">Create Account</h1>
                    <p className="text-stone-500 text-sm">Join Urban Threads today</p>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                    <div className="relative">
                        <User className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} />
                        <input
                            type="text"
                            placeholder="Full Name"
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
                            placeholder="Email Address"
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
                            placeholder="Phone Number (010-0000-0000)"
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
                            placeholder="Address"
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
                            placeholder="Password"
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
                            placeholder="Confirm Password"
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
                        {loading ? 'Creating Account...' : 'Sign Up'}
                    </button>
                </form>

                <div className="mt-8 text-center">
                    <p className="text-stone-500 text-sm">
                        Already have an account?{' '}
                        <button onClick={() => navigate(-1)} className="text-black font-bold hover:underline">
                            Log In
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}

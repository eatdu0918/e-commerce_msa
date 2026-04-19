import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Mail, Lock, AlertCircle } from 'lucide-react';
import { login, getMyProfile } from '../../api/services/user';
import { AUTH_STORAGE_KEYS } from '../../lib/authStorage';

export default function AdminLoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const loginSchema = z.object({
    email: z.string().email(t('auth.email_required', '이메일을 입력해주세요')),
    password: z.string().min(1, t('auth.password_required', '비밀번호를 입력해주세요')),
  });

  type LoginFormValues = z.infer<typeof loginSchema>;

  const { register, handleSubmit, formState: { errors }, setError } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema)
  });

  const onSubmit = async (data: LoginFormValues) => {
    setLoading(true);
    try {
      const response = await login({ email: data.email, password: data.password });
      localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, response.accessToken);
      localStorage.setItem(AUTH_STORAGE_KEYS.refreshToken, response.refreshToken);

      const userProfile = await getMyProfile();
      localStorage.setItem(AUTH_STORAGE_KEYS.user, JSON.stringify(userProfile));
      localStorage.setItem(AUTH_STORAGE_KEYS.role, userProfile.role);

      if (userProfile.role !== 'ADMIN') {
        setError('root', { message: '어드민 권한이 없습니다.' });
        // 권한이 없으므로 토큰 삭제
        localStorage.removeItem(AUTH_STORAGE_KEYS.accessToken);
        localStorage.removeItem(AUTH_STORAGE_KEYS.refreshToken);
        localStorage.removeItem(AUTH_STORAGE_KEYS.user);
        localStorage.removeItem(AUTH_STORAGE_KEYS.role);
        return;
      }

      navigate('/admin', { replace: true });
    } catch (_err: any) {
      setError('root', { message: t('auth.login_failed', '로그인에 실패했습니다.') });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-stone-900 via-stone-800 to-black text-white p-4">
      <div className="w-full max-w-md bg-white/10 backdrop-blur-xl border border-white/20 p-8 rounded-3xl shadow-2xl animate-in fade-in slide-in-from-bottom-8 duration-700">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-white/20 mb-4 shadow-inner ring-1 ring-white/50 relative overflow-hidden group">
            <Lock className="text-white z-10 transition-transform group-hover:scale-110 duration-300" size={28} />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight">UT ADMIN</h1>
          <p className="text-stone-300 mt-2 text-sm">관리자 시스템에 로그인하세요.</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div className="relative">
            <div className="relative group">
              <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300 group-focus-within:text-white transition-colors z-10" size={20} />
              <input
                type="email"
                {...register('email')}
                className={`w-full bg-black/40 border border-white/10 text-white pl-12 pr-4 py-3.5 rounded-xl focus:outline-none focus:ring-2 focus:ring-white/30 focus:border-white/50 transition-all placeholder:text-stone-500 ${errors.email ? 'ring-red-500/50 border-red-500/50' : ''}`}
                placeholder="이메일 주소"
              />
            </div>
            {errors.email && <p className="text-xs text-red-400 mt-1.5 ml-1 flex items-center gap-1"><AlertCircle size={12}/>{errors.email.message}</p>}
          </div>

          <div className="relative">
            <div className="relative group">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300 group-focus-within:text-white transition-colors z-10" size={20} />
              <input
                type="password"
                {...register('password')}
                className={`w-full bg-black/40 border border-white/10 text-white pl-12 pr-4 py-3.5 rounded-xl focus:outline-none focus:ring-2 focus:ring-white/30 focus:border-white/50 transition-all placeholder:text-stone-500 ${errors.password ? 'ring-red-500/50 border-red-500/50' : ''}`}
                placeholder="비밀번호"
              />
            </div>
            {errors.password && <p className="text-xs text-red-400 mt-1.5 ml-1 flex items-center gap-1"><AlertCircle size={12}/>{errors.password.message}</p>}
          </div>

          {errors.root && (
            <div className="bg-red-500/20 border border-red-500/50 text-red-200 text-sm py-3 px-4 rounded-xl flex items-center gap-2 animate-in fade-in duration-300">
              <AlertCircle size={18} className="shrink-0" />
              {errors.root.message}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full relative overflow-hidden bg-white text-black font-bold py-4 rounded-xl shadow-[0_0_20px_rgba(255,255,255,0.3)] hover:shadow-[0_0_30px_rgba(255,255,255,0.5)] transition-all disabled:opacity-70 disabled:cursor-not-allowed group mt-4 hover:-translate-y-0.5"
          >
            <span className="relative z-10">{loading ? '확인 중...' : '로그인'}</span>
            <div className="absolute inset-0 h-full w-0 bg-stone-200 transition-all duration-300 ease-out group-hover:w-full opacity-50 z-0"></div>
          </button>
        </form>

        <div className="mt-8 text-center">
          <button onClick={() => navigate('/')} className="text-stone-400 hover:text-white text-sm transition-colors border-b border-transparent hover:border-white pb-0.5">
            메인 사이트로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}

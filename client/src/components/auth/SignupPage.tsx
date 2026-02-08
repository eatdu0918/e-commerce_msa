import { useState } from 'react';
import { signup } from '../../api/services/user';
import { useTranslation } from 'react-i18next';

interface SignupPageProps {
    onNavigate: (view: string) => void;
    onLoginClick: () => void;
}

export default function SignupPage({ onNavigate, onLoginClick }: SignupPageProps) {
    const { t } = useTranslation();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [gender, setGender] = useState<'MALE' | 'FEMALE'>('MALE');

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    // Validation Regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    const phoneRegex = /^01(?:0|1|[6-9])-(?:\d{3}|\d{4})-\d{4}$/;

    const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value.replace(/[^0-9]/g, '');
        if (value.length <= 11) {
            let formattedValue = value;
            if (value.length > 3 && value.length <= 7) {
                formattedValue = `${value.slice(0, 3)}-${value.slice(3)}`;
            } else if (value.length > 7) {
                formattedValue = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7)}`;
            }
            setPhoneNumber(formattedValue);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (!emailRegex.test(email)) {
            setError('올바른 이메일 형식이 아닙니다.');
            return;
        }
        if (!passwordRegex.test(password)) {
            setError('비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.');
            return;
        }
        if (!phoneRegex.test(phoneNumber)) {
            setError('올바른 휴대폰 번호 형식이 아닙니다. (010-XXXX-XXXX)');
            return;
        }
        if (!name) {
            setError('이름을 입력해주세요.');
            return;
        }

        setLoading(true);
        try {
            await signup({ email, password, name, phoneNumber, gender, role: 'CUSTOMER' });
            alert(t('auth.signup_success', '회원가입이 완료되었습니다. 로그인해주세요.'));
            onLoginClick();
        } catch (err: any) {
            console.error(err);
            // Default error message, ideally parse backend error
            setError(t('auth.signup_failed', '회원가입에 실패했습니다. 입력 정보를 확인해주세요.'));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen pt-32 pb-24 bg-[#f9f7f2] flex flex-col items-center">
            <div className="bg-white rounded-[40px] p-12 w-full max-w-lg shadow-sm border border-stone-100 text-left">
                <h2 className="text-3xl font-bold mb-2">Join Us</h2>
                <p className="text-stone-500 mb-8">URBAN THREADS의 멤버가 되어보세요.</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-1">이메일</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="hello@example.com"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-1">비밀번호</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="8자 이상, 영문/숫자/특수문자 포함"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-1">이름</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="홍길동"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-1">휴대폰 번호</label>
                        <input
                            type="text"
                            value={phoneNumber}
                            onChange={handlePhoneChange}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="010-0000-0000"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-1">성별</label>
                        <div className="flex space-x-4">
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    value="MALE"
                                    checked={gender === 'MALE'}
                                    onChange={() => setGender('MALE')}
                                    className="form-radio text-black focus:ring-black"
                                />
                                <span>남성</span>
                            </label>
                            <label className="flex items-center space-x-2 cursor-pointer">
                                <input
                                    type="radio"
                                    value="FEMALE"
                                    checked={gender === 'FEMALE'}
                                    onChange={() => setGender('FEMALE')}
                                    className="form-radio text-black focus:ring-black"
                                />
                                <span>여성</span>
                            </label>
                        </div>
                    </div>

                    {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50 mt-4"
                    >
                        {loading ? '처리가 중입니다...' : '회원가입'}
                    </button>
                </form>

                <div className="mt-8 text-center text-stone-500">
                    이미 계정이 있으신가요? <button onClick={onLoginClick} className="font-bold text-black underline ml-1">로그인</button>
                </div>
                <div className="mt-4 text-center">
                    <button onClick={() => onNavigate('home')} className="text-sm text-stone-400 hover:text-stone-600 transition-colors">
                        홈으로 돌아가기
                    </button>
                </div>
            </div>
        </div>
    );
}

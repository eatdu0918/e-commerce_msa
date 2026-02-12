import { useState } from 'react';
import { signup } from '../../api/services/user';
import { X } from 'lucide-react';

interface SignupModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSwitchToLogin: () => void;
}

export default function SignupModal({ isOpen, onClose, onSwitchToLogin }: SignupModalProps) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [gender, setGender] = useState<'MALE' | 'FEMALE'>('MALE');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await signup({ email, password, name, phoneNumber, gender });
            alert('회원가입이 완료되었습니다. 로그인해주세요.');
            onSwitchToLogin();
        } catch (_err: any) {
            setError('회원가입에 실패했습니다.');
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

                <h2 className="text-3xl font-bold mb-2">Join Us</h2>
                <p className="text-stone-500 mb-8">URBAN THREADS의 멤버가 되어보세요.</p>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">이름</label>
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
                        <label className="block text-sm font-bold text-stone-700 mb-2">Email</label>
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
                        <label className="block text-sm font-bold text-stone-700 mb-2">Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="영문+숫자+특수문자 8자 이상"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">휴대폰 번호</label>
                        <input
                            type="tel"
                            value={phoneNumber}
                            onChange={(e) => setPhoneNumber(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="010-0000-0000"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">성별</label>
                        <div className="grid grid-cols-2 gap-3">
                            <button
                                type="button"
                                onClick={() => setGender('MALE')}
                                className={`py-3 rounded-xl border text-sm font-medium transition-all ${gender === 'MALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                            >
                                남성
                            </button>
                            <button
                                type="button"
                                onClick={() => setGender('FEMALE')}
                                className={`py-3 rounded-xl border text-sm font-medium transition-all ${gender === 'FEMALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                            >
                                여성
                            </button>
                        </div>
                    </div>

                    {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? 'Processing...' : 'Sign Up'}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-stone-500">
                    이미 계정이 있으신가요? <button onClick={onSwitchToLogin} className="font-bold text-black underline">로그인</button>
                </div>
            </div>
        </div>
    );
}

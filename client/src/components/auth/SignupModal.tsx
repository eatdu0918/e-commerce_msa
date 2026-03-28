import { useState } from 'react';
import { signup } from '../../api/services/user';
import { X } from 'lucide-react';
import { useScrollLock } from '../../hooks/useScrollLock';
import { useTranslation } from 'react-i18next';

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

    useScrollLock(isOpen);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await signup({ email, password, name, phoneNumber, gender, address: '', role: 'USER' });
            alert(t('signupModal.success_alert'));
            onSwitchToLogin();
        } catch (_err: any) {
            setError(t('signupModal.fail_error'));
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

                <h2 className="text-3xl font-bold mb-2">{t('signupModal.join_title')}</h2>
                <p className="text-stone-500 mb-8">{t('signupModal.subtitle')}</p>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('signupModal.name_label')}</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('checkout.placeholder_name')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('auth.email')}</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('auth.email_placeholder')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('auth.password')}</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('signupModal.password_pattern_hint')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('signupModal.phone_label')}</label>
                        <input
                            type="tel"
                            value={phoneNumber}
                            onChange={(e) => setPhoneNumber(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('signupModal.phone_placeholder')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('signupModal.gender_label')}</label>
                        <div className="grid grid-cols-2 gap-3">
                            <button
                                type="button"
                                onClick={() => setGender('MALE')}
                                className={`py-3 rounded-xl border text-sm font-medium transition-all ${gender === 'MALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                            >
                                {t('profile.male')}
                            </button>
                            <button
                                type="button"
                                onClick={() => setGender('FEMALE')}
                                className={`py-3 rounded-xl border text-sm font-medium transition-all ${gender === 'FEMALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                            >
                                {t('profile.female')}
                            </button>
                        </div>
                    </div>

                    {error && <p className="text-red-500 text-sm font-medium">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? t('auth.processing') : t('auth.signup')}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-stone-500">
                    {t('signupModal.has_account')}{' '}
                    <button type="button" onClick={onSwitchToLogin} className="font-bold text-black underline">
                        {t('auth.log_in')}
                    </button>
                </div>
            </div>
        </div>
    );
}

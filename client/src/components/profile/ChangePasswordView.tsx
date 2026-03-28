import React, { useState } from 'react';
import { Lock, ArrowLeft, Eye, EyeOff } from 'lucide-react';
import { changePassword } from '../../api/services/user';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function ChangePasswordView() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const [showCurrent, setShowCurrent] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [validationError, setValidationError] = useState('');

    const mutation = useMutation({
        mutationFn: () =>
            changePassword({
                currentPassword: form.currentPassword,
                newPassword: form.newPassword,
            }),
        onSuccess: () => {
            toast.success(t('password.toast_ok'));
            navigate('/me');
        },
        onError: () => {
            toast.error(t('password.toast_fail'));
        },
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setValidationError('');

        if (form.newPassword.length < 8) {
            setValidationError(t('password.min_length'));
            return;
        }
        if (form.newPassword !== form.confirmPassword) {
            setValidationError(t('password.mismatch'));
            return;
        }
        if (form.currentPassword === form.newPassword) {
            setValidationError(t('password.same_as_current'));
            return;
        }

        mutation.mutate();
    };

    const PasswordField = ({
        label,
        value,
        onChange,
        show,
        onToggle,
        placeholder,
    }: {
        label: string;
        value: string;
        onChange: (v: string) => void;
        show: boolean;
        onToggle: () => void;
        placeholder?: string;
    }) => (
        <div className="space-y-1.5">
            <label className="text-[11px] font-bold text-stone-400 uppercase tracking-widest px-1">
                {label}
            </label>
            <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300" size={18} />
                <input
                    type={show ? 'text' : 'password'}
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    className="w-full bg-white border border-stone-200 rounded-2xl py-4 pl-12 pr-12 text-sm focus:ring-2 focus:ring-black/5 focus:border-black transition-all outline-none"
                    placeholder={placeholder}
                    required
                />
                <button
                    type="button"
                    onClick={onToggle}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-stone-300 hover:text-stone-500 transition-colors"
                >
                    {show ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
            </div>
        </div>
    );

    return (
        <div className="max-w-xl mx-auto">
            <div className="flex items-center mb-8">
                <button
                    onClick={() => navigate(-1)}
                    className="p-2 hover:bg-stone-100 rounded-full transition-colors mr-2"
                >
                    <ArrowLeft size={24} />
                </button>
                <h1 className="text-2xl font-bold tracking-tight">{t('password.title')}</h1>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6 text-left">
                <PasswordField
                    label={t('password.current')}
                    value={form.currentPassword}
                    onChange={(v) => setForm({ ...form, currentPassword: v })}
                    show={showCurrent}
                    onToggle={() => setShowCurrent(!showCurrent)}
                    placeholder={t('password.placeholder_current')}
                />
                <PasswordField
                    label={t('password.new')}
                    value={form.newPassword}
                    onChange={(v) => setForm({ ...form, newPassword: v })}
                    show={showNew}
                    onToggle={() => setShowNew(!showNew)}
                    placeholder={t('password.placeholder_new')}
                />
                <PasswordField
                    label={t('password.confirm')}
                    value={form.confirmPassword}
                    onChange={(v) => setForm({ ...form, confirmPassword: v })}
                    show={showConfirm}
                    onToggle={() => setShowConfirm(!showConfirm)}
                    placeholder={t('password.placeholder_confirm')}
                />

                {validationError && (
                    <p className="text-red-500 text-sm font-medium bg-red-50 py-2 px-3 rounded-lg">
                        {validationError}
                    </p>
                )}

                <div className="pt-6 flex space-x-3">
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        className="flex-1 py-4 bg-stone-100 text-stone-600 rounded-2xl text-sm font-bold hover:bg-stone-200 transition-colors"
                    >
                        {t('password.cancel')}
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending}
                        className="flex-2 bg-stone-900 text-white rounded-2xl py-4 px-12 text-sm font-bold hover:bg-black transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {mutation.isPending ? t('password.submitting') : t('password.submit')}
                    </button>
                </div>
            </form>
        </div>
    );
}

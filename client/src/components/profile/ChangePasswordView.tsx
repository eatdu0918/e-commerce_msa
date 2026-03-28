import React, { useState } from 'react';
import { Lock, ArrowLeft, Eye, EyeOff } from 'lucide-react';
import { changePassword } from '../../api/services/user';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

export default function ChangePasswordView() {
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
            toast.success('비밀번호가 변경되었습니다.');
            navigate('/me');
        },
        onError: () => {
            toast.error('비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.');
        },
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setValidationError('');

        if (form.newPassword.length < 8) {
            setValidationError('새 비밀번호는 8자 이상이어야 합니다.');
            return;
        }
        if (form.newPassword !== form.confirmPassword) {
            setValidationError('새 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (form.currentPassword === form.newPassword) {
            setValidationError('현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.');
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
                <h1 className="text-2xl font-bold tracking-tight">비밀번호 변경</h1>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6 text-left">
                <PasswordField
                    label="현재 비밀번호"
                    value={form.currentPassword}
                    onChange={(v) => setForm({ ...form, currentPassword: v })}
                    show={showCurrent}
                    onToggle={() => setShowCurrent(!showCurrent)}
                    placeholder="현재 비밀번호 입력"
                />
                <PasswordField
                    label="새 비밀번호"
                    value={form.newPassword}
                    onChange={(v) => setForm({ ...form, newPassword: v })}
                    show={showNew}
                    onToggle={() => setShowNew(!showNew)}
                    placeholder="새 비밀번호 (8자 이상)"
                />
                <PasswordField
                    label="새 비밀번호 확인"
                    value={form.confirmPassword}
                    onChange={(v) => setForm({ ...form, confirmPassword: v })}
                    show={showConfirm}
                    onToggle={() => setShowConfirm(!showConfirm)}
                    placeholder="새 비밀번호 재입력"
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
                        취소
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending}
                        className="flex-2 bg-stone-900 text-white rounded-2xl py-4 px-12 text-sm font-bold hover:bg-black transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {mutation.isPending ? '변경 중...' : '비밀번호 변경'}
                    </button>
                </div>
            </form>
        </div>
    );
}

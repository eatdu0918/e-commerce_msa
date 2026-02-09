import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { User, Phone, Mail, ChevronLeft } from 'lucide-react';
import type { UserResponse, UpdateProfileRequest } from '../../api/services/user';
import { updateProfile } from '../../api/services/user';
import { useMutation, useQueryClient } from '@tanstack/react-query';

interface EditProfileViewProps {
    user: UserResponse;
    onBack: () => void;
}

export default function EditProfileView({ user, onBack }: EditProfileViewProps) {
    const { t } = useTranslation();
    const queryClient = useQueryClient();

    const [form, setForm] = useState<UpdateProfileRequest>({
        username: user.username,
        phoneNumber: '', // Back-end should probably provide this, assuming empty for now or mock
        gender: 'MALE'
    });

    const mutation = useMutation({
        mutationFn: updateProfile,
        onSuccess: (data) => {
            queryClient.setQueryData(['user'], data);
            alert(t('profile.success'));
            onBack();
        },
        onError: () => {
            alert(t('profile.error'));
        }
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate(form);
    };

    return (
        <div className="max-w-xl mx-auto">
            <div className="flex items-center mb-8">
                <button
                    onClick={onBack}
                    className="p-2 hover:bg-stone-100 rounded-full transition-colors mr-2"
                >
                    <ChevronLeft size={20} />
                </button>
                <h3 className="text-2xl font-bold">{t('profile.title')}</h3>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6 text-left">
                {/* Email (Read-only) */}
                <div className="space-y-1.5">
                    <label className="text-[11px] font-bold text-stone-400 uppercase tracking-widest px-1">
                        {t('profile.email')}
                    </label>
                    <div className="relative">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300" size={18} />
                        <input
                            type="email"
                            value={user.email}
                            disabled
                            className="w-full bg-stone-50 border border-stone-200 rounded-2xl py-4 pl-12 pr-4 text-stone-400 cursor-not-allowed text-sm focus:outline-none"
                        />
                    </div>
                </div>

                {/* Name */}
                <div className="space-y-1.5">
                    <label className="text-[11px] font-bold text-stone-400 uppercase tracking-widest px-1">
                        {t('profile.name')}
                    </label>
                    <div className="relative">
                        <User className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300" size={18} />
                        <input
                            type="text"
                            value={form.username}
                            onChange={(e) => setForm({ ...form, username: e.target.value })}
                            className="w-full bg-white border border-stone-200 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-2 focus:ring-black/5 focus:border-black transition-all outline-none"
                            placeholder={t('profile.name')}
                            required
                        />
                    </div>
                </div>

                {/* Phone */}
                <div className="space-y-1.5">
                    <label className="text-[11px] font-bold text-stone-400 uppercase tracking-widest px-1">
                        {t('profile.phone')}
                    </label>
                    <div className="relative">
                        <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-300" size={18} />
                        <input
                            type="tel"
                            value={form.phoneNumber}
                            onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                            className="w-full bg-white border border-stone-200 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-2 focus:ring-black/5 focus:border-black transition-all outline-none"
                            placeholder="010-0000-0000"
                        />
                    </div>
                </div>

                {/* Gender */}
                <div className="space-y-1.5">
                    <label className="text-[11px] font-bold text-stone-400 uppercase tracking-widest px-1">
                        {t('profile.gender')}
                    </label>
                    <div className="grid grid-cols-2 gap-3">
                        <button
                            type="button"
                            onClick={() => setForm({ ...form, gender: 'MALE' })}
                            className={`py-4 rounded-2xl border text-sm font-medium transition-all ${form.gender === 'MALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                        >
                            {t('profile.male')}
                        </button>
                        <button
                            type="button"
                            onClick={() => setForm({ ...form, gender: 'FEMALE' })}
                            className={`py-4 rounded-2xl border text-sm font-medium transition-all ${form.gender === 'FEMALE'
                                    ? 'bg-black text-white border-black'
                                    : 'bg-white text-stone-400 border-stone-200 hover:border-stone-400'
                                }`}
                        >
                            {t('profile.female')}
                        </button>
                    </div>
                </div>

                <div className="pt-6 flex space-x-3">
                    <button
                        type="button"
                        onClick={onBack}
                        className="flex-1 py-4 bg-stone-100 text-stone-600 rounded-2xl text-sm font-bold hover:bg-stone-200 transition-colors"
                    >
                        {t('profile.cancel')}
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending}
                        className="flex-2 bg-stone-900 text-white rounded-2xl py-4 px-12 text-sm font-bold hover:bg-black transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {mutation.isPending ? t('common.adding') : t('profile.save')}
                    </button>
                </div>
            </form>
        </div>
    );
}

import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { User, Phone, Mail, ArrowLeft } from 'lucide-react';
import type { UserResponse, UpdateProfileRequest } from '../../api/services/user';
import { updateProfile } from '../../api/services/user';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

export default function EditProfileView() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const user = queryClient.getQueryData<UserResponse>(['user']);

    const [form, setForm] = useState<UpdateProfileRequest>({
        name: '',
        phoneNumber: '',
        address: '',
        gender: 'MALE',
    });

    useEffect(() => {
        if (user) {
            setForm({
                name: user.name || '',
                phoneNumber: user.phoneNumber || '',
                address: user.address || '',
                gender: user.gender || 'MALE',
            });
        }
    }, [user]);

    const mutation = useMutation({
        mutationFn: (data: UpdateProfileRequest) => updateProfile(data),
        onSuccess: (data) => {
            queryClient.setQueryData(['user'], data);
            toast.success(t('profile.success') || 'Profile updated successfully');
            navigate(-1);
        },
        onError: () => {
            toast.error(t('profile.error') || 'Failed to update profile');
        }
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate(form);
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="max-w-xl mx-auto">
            <div className="flex items-center mb-8">
                <button
                    onClick={() => navigate(-1)}
                    className="p-2 hover:bg-stone-100 rounded-full transition-colors mr-2"
                >
                    <ArrowLeft size={24} />
                </button>
                <h1 className="text-2xl font-bold tracking-tight">{t('common.edit_profile')}</h1>
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
                            value={form.name}
                            onChange={(e) => setForm({ ...form, name: e.target.value })}
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
                        onClick={() => navigate(-1)}
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

import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createReview } from '../../api/services/review';
import { useTranslation } from 'react-i18next';
import { Star } from 'lucide-react';
import toast from 'react-hot-toast';
import type { CreateReviewRequest } from '../../api/services/review';

interface ReviewFormProps {
    productId: number;
    onCancel?: () => void;
}

export default function ReviewForm({ productId, onCancel }: ReviewFormProps) {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const [content, setContent] = useState('');
    const [score, setScore] = useState(5);
    const [hoverScore, setHoverScore] = useState(0);

    const mutation = useMutation({
        mutationFn: (data: CreateReviewRequest) => createReview(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['reviews', productId] });
            toast.success(t('review.success') || 'Review submitted successfully');
            setContent('');
            setScore(5);
            if (onCancel) onCancel();
        },
        onError: () => {
            toast.error(t('review.error') || 'Failed to submit review');
        },
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const trimmedContent = content.trim();
        
        if (!trimmedContent) return;

        if (trimmedContent.length < 5) {
            toast.error(t('review.content_length_error'));
            return;
        }

        mutation.mutate({
            productId,
            score,
            content: trimmedContent
        });
    };

    return (
        <form onSubmit={handleSubmit} className="bg-stone-50 p-6 rounded-2xl border border-stone-100 mb-8">
            <h4 className="font-bold mb-4">{t('review.write_title')}</h4>

            <div className="mb-4">
                <label className="block text-xs font-bold text-stone-400 uppercase mb-2">{t('review.rating')}</label>
                <div className="flex space-x-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                        <button
                            key={star}
                            type="button"
                            onClick={() => setScore(star)}
                            onMouseEnter={() => setHoverScore(star)}
                            onMouseLeave={() => setHoverScore(0)}
                            className="focus:outline-none transition-transform hover:scale-110"
                        >
                            <Star
                                size={24}
                                fill={(hoverScore || score) >= star ? "currentColor" : "none"}
                                className={(hoverScore || score) >= star ? "text-yellow-400" : "text-stone-300"}
                            />
                        </button>
                    ))}
                </div>
            </div>

            <div className="mb-4">
                <label className="block text-xs font-bold text-stone-400 uppercase mb-2">{t('review.content')}</label>
                <textarea
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder={t('review.placeholder')}
                    className="w-full bg-white border border-stone-200 rounded-xl p-4 text-sm focus:ring-2 focus:ring-black/5 focus:border-black transition-all outline-none resize-none h-32"
                    required
                />
            </div>

            <div className="flex justify-end space-x-3">
                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        className="px-6 py-2 bg-stone-200 text-stone-600 rounded-xl text-sm font-bold hover:bg-stone-300 transition-colors"
                    >
                        {t('review.cancel')}
                    </button>
                )}
                <button
                    type="submit"
                    disabled={mutation.isPending || !content.trim()}
                    className="px-6 py-2 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-colors disabled:opacity-50"
                >
                    {mutation.isPending ? '...' : t('review.submit')}
                </button>
            </div>
        </form>
    );
}

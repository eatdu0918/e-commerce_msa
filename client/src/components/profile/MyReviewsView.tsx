import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyReviews, updateReview, deleteReview } from '../../api/services/review';
import type { ReviewResponse } from '../../api/services/review';
import { Star, Edit2, Trash2, X, Check } from 'lucide-react';
import dayjs from 'dayjs';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';

export default function MyReviewsView() {
    const { t } = useTranslation();
    const [page, setPage] = useState(0);
    const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null);
    const [editScore, setEditScore] = useState(5);
    const [editContent, setEditContent] = useState('');
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ['my-reviews', page],
        queryFn: () => getMyReviews(page, 10),
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: { score: number; content: string } }) =>
            updateReview(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['my-reviews'] });
            setEditingReview(null);
            toast.success(t('review.success'));
        },
    });

    const deleteMutation = useMutation({
        mutationFn: deleteReview,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['my-reviews'] });
            toast.success(t('common.delete_success') || 'Deleted successfully');
        },
    });

    const handleEdit = (review: ReviewResponse) => {
        setEditingReview(review);
        setEditScore(review.score);
        setEditContent(review.content);
    };

    const handleUpdate = () => {
        if (!editingReview) return;
        updateMutation.mutate({
            id: editingReview.id,
            data: { score: editScore, content: editContent },
        });
    };

    const handleDelete = (reviewId: number) => {
        if (window.confirm(t('review.delete_confirm'))) {
            deleteMutation.mutate(reviewId);
        }
    };

    if (isLoading) {
        return (
            <div className="max-w-5xl mx-auto px-6 py-12 space-y-4">
                {[...Array(3)].map((_, i) => (
                    <div key={i} className="bg-white rounded-2xl p-6 animate-pulse">
                        <div className="h-4 bg-stone-100 rounded w-1/3 mb-3" />
                        <div className="h-3 bg-stone-100 rounded w-full mb-2" />
                        <div className="h-3 bg-stone-100 rounded w-2/3" />
                    </div>
                ))}
            </div>
        );
    }

    const reviews = data?.content || [];

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold">{t('review.my_reviews_title')}</h1>
                    <p className="text-stone-500 text-sm mt-1">{t('review.my_reviews_subtitle')}</p>
                </div>
                <span className="text-sm text-stone-400">
                    {t('review.total_count', { count: data?.totalElements || 0 })}
                </span>
            </div>

            {reviews.length === 0 ? (
                <div className="bg-white rounded-2xl p-12 text-center border border-stone-100">
                    <p className="text-stone-400">{t('review.empty_msg')}</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {reviews.map((review) => (
                        <div
                            key={review.id}
                            className="bg-white rounded-2xl p-6 border border-stone-100 shadow-sm text-left"
                        >
                            {editingReview?.id === review.id ? (
                                // Edit Mode
                                <div className="space-y-4">
                                    <div>
                                        <label className="text-xs font-bold text-stone-400 uppercase mb-2 block">
                                            {t('review.rating')}
                                        </label>
                                        <div className="flex space-x-1">
                                            {[1, 2, 3, 4, 5].map((star) => (
                                                <button
                                                    key={star}
                                                    onClick={() => setEditScore(star)}
                                                    className="focus:outline-none"
                                                >
                                                    <Star
                                                        size={24}
                                                        fill={star <= editScore ? '#facc15' : 'none'}
                                                        className={star <= editScore ? 'text-yellow-400' : 'text-stone-200'}
                                                    />
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                    <div>
                                        <label className="text-xs font-bold text-stone-400 uppercase mb-2 block">
                                            {t('review.content')}
                                        </label>
                                        <textarea
                                            value={editContent}
                                            onChange={(e) => setEditContent(e.target.value)}
                                            className="w-full border border-stone-200 rounded-xl p-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none resize-none"
                                            rows={3}
                                        />
                                    </div>
                                    <div className="flex justify-end space-x-2">
                                        <button
                                            onClick={() => setEditingReview(null)}
                                            className="px-4 py-2 text-sm text-stone-500 hover:bg-stone-100 rounded-lg transition-colors flex items-center space-x-1"
                                        >
                                            <X size={16} />
                                            <span>{t('review.cancel')}</span>
                                        </button>
                                        <button
                                            onClick={handleUpdate}
                                            disabled={updateMutation.isPending}
                                            className="px-4 py-2 text-sm bg-black text-white rounded-lg hover:bg-stone-800 transition-colors flex items-center space-x-1 disabled:opacity-50"
                                        >
                                            <Check size={16} />
                                            <span>{updateMutation.isPending ? t('review.saving') : t('review.save')}</span>
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                // View Mode
                                <>
                                    <div className="flex justify-between items-start mb-3">
                                        <div>
                                            <p className="text-xs text-stone-400 mb-1">
                                                {t('review.product_no', { id: review.productId })}
                                            </p>
                                            <div className="flex items-center space-x-2">
                                                <div className="flex">
                                                    {[1, 2, 3, 4, 5].map((star) => (
                                                        <Star
                                                            key={star}
                                                            size={16}
                                                            fill={star <= review.score ? '#facc15' : 'none'}
                                                            className={star <= review.score ? 'text-yellow-400' : 'text-stone-200'}
                                                        />
                                                    ))}
                                                </div>
                                                <span className="text-xs text-stone-400">
                                                    {dayjs(review.createdAt).format('YYYY-MM-DD')}
                                                </span>
                                            </div>
                                        </div>
                                        <div className="flex space-x-1">
                                            <button
                                                onClick={() => handleEdit(review)}
                                                className="p-2 text-stone-400 hover:text-black hover:bg-stone-100 rounded-lg transition-colors"
                                            >
                                                <Edit2 size={16} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(review.id)}
                                                disabled={deleteMutation.isPending}
                                                className="p-2 text-stone-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </div>
                                    <p className="text-stone-600 text-sm leading-relaxed">{review.content}</p>
                                    {review.imageUrl && (
                                        <div className="mt-3">
                                            <img
                                                src={review.imageUrl}
                                                alt="Review"
                                                className="w-20 h-20 object-cover rounded-lg border border-stone-100"
                                            />
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            {data && data.totalPages > 1 && (
                <div className="flex justify-center space-x-2 pt-4">
                    <button
                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="px-4 py-2 rounded-lg border border-stone-200 text-sm disabled:opacity-30 hover:bg-stone-50 transition-colors"
                    >
                        {t('review.prev')}
                    </button>
                    <span className="px-4 py-2 text-sm text-stone-500">
                        {t('review.page_info', { current: page + 1, total: data.totalPages })}
                    </span>
                    <button
                        onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                        disabled={page >= data.totalPages - 1}
                        className="px-4 py-2 rounded-lg border border-stone-200 text-sm disabled:opacity-30 hover:bg-stone-50 transition-colors"
                    >
                        {t('review.next')}
                    </button>
                </div>
            )}
        </div>
    );
}

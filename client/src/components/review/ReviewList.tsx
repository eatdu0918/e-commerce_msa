import { useQuery } from '@tanstack/react-query';
import { getProductReviews } from '../../api/services/review';
import { Star, User } from 'lucide-react';
import { format } from 'date-fns';

interface ReviewListProps {
    productId: number;
}

export default function ReviewList({ productId }: ReviewListProps) {
    const { data: reviewsPage, isLoading } = useQuery({
        queryKey: ['reviews', productId],
        queryFn: () => getProductReviews(productId, 0, 10),
    });

    const reviews = reviewsPage?.content || [];

    if (isLoading) return <div className="text-center py-10">Loading reviews...</div>;

    if (reviews.length === 0) {
        return (
            <div className="text-center py-12 bg-stone-50 rounded-2xl border border-stone-100">
                <p className="text-stone-400">No reviews yet. Be the first to write one!</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <h3 className="text-xl font-bold">Reviews ({reviewsPage?.totalElements})</h3>
            <div className="space-y-6">
                {reviews.map((review) => (
                    <div key={review.id} className="border-b border-stone-100 pb-6 last:border-0">
                        <div className="flex justify-between items-start mb-2">
                            <div className="flex items-center space-x-2">
                                <div className="w-8 h-8 bg-stone-100 rounded-full flex items-center justify-center">
                                    <User size={14} className="text-stone-400" />
                                </div>
                                <div>
                                    <p className="text-sm font-bold">{review.userName || 'Anonymous'}</p>
                                    <div className="flex text-yellow-500">
                                        {[...Array(5)].map((_, i) => (
                                            <Star
                                                key={i}
                                                size={12}
                                                fill={i < review.score ? "currentColor" : "none"}
                                                className={i < review.score ? "text-yellow-400" : "text-stone-200"}
                                            />
                                        ))}
                                    </div>
                                </div>
                            </div>
                            <span className="text-xs text-stone-400">
                                {format(new Date(review.createdAt), 'yyyy-MM-dd')}
                            </span>
                        </div>
                        <p className="text-stone-600 text-sm mt-3 leading-relaxed">{review.content}</p>
                        {review.imageUrl && (
                            <div className="mt-3">
                                <img src={review.imageUrl} alt="Review attachment" className="w-24 h-24 object-cover rounded-lg border border-stone-100" />
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

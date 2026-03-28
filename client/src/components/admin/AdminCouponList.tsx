import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi, Coupon, CreateCouponRequest } from '../../api/services/admin';
import { ChevronLeft, ChevronRight, Trash2, Plus, X } from 'lucide-react';

const AdminCouponList = () => {
  const { t, i18n } = useTranslation();
  const [page, setPage] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState<CreateCouponRequest>({
    code: '',
    name: '',
    discountType: 'PERCENTAGE',
    discountValue: 0,
    minimumPurchaseAmount: 0,
    maxDiscountAmount: 0,
    validFrom: '',
    validUntil: '',
  });

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-coupons', page],
    queryFn: () => adminApi.getCoupons(page, 10),
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateCouponRequest) => adminApi.createCoupon(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
      alert(t('admin.coupon_create_ok'));
      setShowModal(false);
      resetForm();
    },
    onError: () => {
      alert(t('admin.coupon_create_fail'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteCoupon(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-coupons'] });
      alert(t('admin.coupon_deleted'));
    },
    onError: () => {
      alert(t('admin.coupon_delete_fail'));
    },
  });

  const resetForm = () => {
    setFormData({
      code: '',
      name: '',
      discountType: 'PERCENTAGE',
      discountValue: 0,
      minimumPurchaseAmount: 0,
      maxDiscountAmount: 0,
      validFrom: '',
      validUntil: '',
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(formData);
  };

  const handleDelete = (id: number, code: string) => {
    if (window.confirm(t('admin.confirm_delete_coupon', { code }))) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">{t('admin.loading')}</div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-red-500">{t('admin.error_load')}</div>
      </div>
    );
  }

  const coupons = data?.data?.content || [];
  const pageData = data?.data;
  const dateLocale = i18n.language.startsWith('ko') ? 'ko-KR' : undefined;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{t('admin.coupons_title')}</h2>
          <p className="text-gray-600 mt-1">
            {t('admin.total_count', { count: pageData?.totalElements || 0 })}
          </p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          <Plus className="w-4 h-4 mr-2" />
          {t('admin.create_coupon')}
        </button>
      </div>

      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.code')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.name')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.discount')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.min_purchase')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.valid_range')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.status')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.actions')}
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {coupons.map((coupon: Coupon) => (
              <tr key={coupon.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {coupon.code}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {coupon.name}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {coupon.discountType === 'PERCENTAGE'
                    ? `${coupon.discountValue ?? 0}%`
                    : `${coupon.discountValue?.toLocaleString() ?? '-'}${t('common.currency_won')}`}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {coupon.minimumPurchaseAmount?.toLocaleString() ?? '-'}{t('common.currency_won')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(coupon.validFrom).toLocaleDateString(dateLocale)} ~{' '}
                  {new Date(coupon.validUntil).toLocaleDateString(dateLocale)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      coupon.isActive
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {coupon.isActive ? t('admin.active') : t('admin.inactive')}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <button
                    onClick={() => handleDelete(coupon.id, coupon.code)}
                    className="text-red-600 hover:text-red-900"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between mt-6">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={pageData?.first}
          className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <ChevronLeft className="w-4 h-4 mr-1" />
          {t('admin.prev')}
        </button>
        <span className="text-sm text-gray-700">
          {page + 1} / {pageData?.totalPages || 1}
        </span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={pageData?.last}
          className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {t('admin.next')}
          <ChevronRight className="w-4 h-4 ml-1" />
        </button>
      </div>

      {/* Create Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">{t('admin.modal_create_title')}</h3>
              <button onClick={() => setShowModal(false)}>
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.coupon_code')}
                </label>
                <input
                  type="text"
                  required
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.coupon_name')}
                </label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.discount_type')}
                </label>
                <select
                  value={formData.discountType}
                  onChange={(e) =>
                    setFormData({ ...formData, discountType: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="PERCENTAGE">{t('admin.type_percent')}</option>
                  <option value="FIXED">{t('admin.type_fixed')}</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.discount_value')}
                </label>
                <input
                  type="number"
                  required
                  value={formData.discountValue}
                  onChange={(e) =>
                    setFormData({ ...formData, discountValue: Number(e.target.value) })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.min_purchase')}
                </label>
                <input
                  type="number"
                  required
                  value={formData.minimumPurchaseAmount}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      minimumPurchaseAmount: Number(e.target.value),
                    })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.max_discount')}
                </label>
                <input
                  type="number"
                  required
                  value={formData.maxDiscountAmount}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      maxDiscountAmount: Number(e.target.value),
                    })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.start_date')}
                </label>
                <input
                  type="datetime-local"
                  required
                  value={formData.validFrom}
                  onChange={(e) =>
                    setFormData({ ...formData, validFrom: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {t('admin.end_date')}
                </label>
                <input
                  type="datetime-local"
                  required
                  value={formData.validUntil}
                  onChange={(e) =>
                    setFormData({ ...formData, validUntil: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <button
                type="submit"
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                {t('admin.create_submit')}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCouponList;

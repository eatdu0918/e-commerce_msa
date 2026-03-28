import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi, Refund } from '../../api/services/admin';
import { ChevronLeft, ChevronRight, RotateCcw } from 'lucide-react';

const AdminRefundList = () => {
  const { t, i18n } = useTranslation();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-refunds', page, statusFilter],
    queryFn: () => adminApi.getRefunds(page, 10, statusFilter || undefined),
  });

  const retryMutation = useMutation({
    mutationFn: (id: number) => adminApi.retryRefund(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-refunds'] });
      alert(t('admin.refund_retry_ok'));
    },
    onError: () => {
      alert(t('admin.refund_retry_fail'));
    },
  });

  const handleRetry = (id: number) => {
    if (window.confirm(t('admin.confirm_refund_retry'))) {
      retryMutation.mutate(id);
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

  const refunds = data?.data?.content || [];
  const pageData = data?.data;

  const statusOptions = ['PENDING', 'COMPLETED', 'FAILED'];
  const dateLocale = i18n.language.startsWith('ko') ? 'ko-KR' : undefined;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{t('admin.refunds_title')}</h2>
          <p className="text-gray-600 mt-1">
            {t('admin.total_items', { count: pageData?.totalElements || 0 })}
          </p>
        </div>
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          className="px-4 py-2 border border-gray-300 rounded-lg"
        >
          <option value="">{t('admin.all_status')}</option>
          {statusOptions.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>

      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.refund_id')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.cancel_id')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.order_id')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.refund_amount')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.status')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.created')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.updated')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.actions')}
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {refunds.map((refund: Refund) => (
              <tr key={refund.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  #{refund.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  #{refund.cancelId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  #{refund.orderId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {refund.amount?.toLocaleString() ?? '-'}{t('common.currency_won')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      refund.status === 'COMPLETED'
                        ? 'bg-green-100 text-green-800'
                        : refund.status === 'FAILED'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {refund.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(refund.createdAt).toLocaleDateString(dateLocale)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(refund.updatedAt).toLocaleDateString(dateLocale)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {refund.status === 'FAILED' && (
                    <button
                      onClick={() => handleRetry(refund.id)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded"
                      title={t('admin.retry_title')}
                    >
                      <RotateCcw className="w-4 h-4" />
                    </button>
                  )}
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
    </div>
  );
};

export default AdminRefundList;

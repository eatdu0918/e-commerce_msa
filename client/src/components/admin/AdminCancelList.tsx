import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi, Cancel } from '../../api/services/admin';
import { ChevronLeft, ChevronRight, Check, X } from 'lucide-react';

const AdminCancelList = () => {
  const { t, i18n } = useTranslation();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [rejectingId, setRejectingId] = useState<number | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-cancels', page, statusFilter],
    queryFn: () => adminApi.getCancels(page, 10, statusFilter || undefined),
  });

  const approveMutation = useMutation({
    mutationFn: (id: number) => adminApi.approveCancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      alert(t('admin.approve_ok'));
    },
    onError: () => {
      alert(t('admin.approve_fail'));
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      adminApi.rejectCancel(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      alert(t('admin.reject_ok'));
      setRejectingId(null);
      setRejectReason('');
    },
    onError: () => {
      alert(t('admin.reject_fail'));
    },
  });

  const handleApprove = (id: number) => {
    if (window.confirm(t('admin.confirm_approve'))) {
      approveMutation.mutate(id);
    }
  };

  const handleReject = (id: number) => {
    if (!rejectReason.trim()) {
      alert(t('admin.reject_reason_required'));
      return;
    }
    rejectMutation.mutate({ id, reason: rejectReason });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">{t('admin.loading')}</div>
      </div>
    );
  }

  const cancels = data?.data?.content || [];
  const pageData = data?.data;

  const statusOptions = ['REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED'];
  const dateLocale = i18n.language.startsWith('ko') ? 'ko-KR' : undefined;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{t('admin.cancels_title')}</h2>
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
                {t('admin.cancel_id_col')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.order_id')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.user_id')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.cancel_reason')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.status')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.requested_at')}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                {t('admin.actions')}
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {cancels.map((cancel: Cancel) => (
              <tr key={cancel.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  #{cancel.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  #{cancel.orderId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {cancel.userId}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900 max-w-xs truncate">
                  {cancel.cancelReason}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      cancel.status === 'APPROVED'
                        ? 'bg-green-100 text-green-800'
                        : cancel.status === 'REJECTED'
                        ? 'bg-red-100 text-red-800'
                        : cancel.status === 'COMPLETED'
                        ? 'bg-blue-100 text-blue-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {cancel.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(cancel.requestedAt).toLocaleDateString(dateLocale)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {cancel.status === 'REQUESTED' && (
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleApprove(cancel.id)}
                        className="p-2 text-green-600 hover:bg-green-50 rounded"
                        title={t('admin.approve_title')}
                      >
                        <Check className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => setRejectingId(cancel.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded"
                        title={t('admin.reject_title')}
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
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

      {/* Reject Modal */}
      {rejectingId && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">{t('admin.reject_modal_title')}</h3>
              <button onClick={() => setRejectingId(null)}>
                <X className="w-5 h-5" />
              </button>
            </div>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder={t('admin.reject_placeholder')}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg h-32 resize-none"
            />
            <div className="flex space-x-3 mt-4">
              <button
                onClick={() => setRejectingId(null)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                {t('admin.cancel_btn')}
              </button>
              <button
                onClick={() => handleReject(rejectingId)}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                {t('admin.reject_btn')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCancelList;

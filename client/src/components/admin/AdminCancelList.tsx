import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi, type Cancel } from '../../api/services/admin';
import { ChevronLeft, ChevronRight, Check, X, Eye } from 'lucide-react';

function normalizeCancelStatus(raw: unknown): string {
  if (raw == null) return '';
  if (typeof raw === 'string') return raw.toUpperCase();
  if (typeof raw === 'object' && 'name' in (raw as object)) {
    const name = (raw as { name?: unknown }).name;
    return typeof name === 'string' ? name.toUpperCase() : '';
  }
  return String(raw).toUpperCase();
}

const AdminCancelList = () => {
  const { t, i18n } = useTranslation();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [rejectingId, setRejectingId] = useState<number | null>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-cancels', page, statusFilter],
    queryFn: () => adminApi.getCancels(page, 10, statusFilter || undefined),
  });

  const {
    data: detailWrap,
    isLoading: detailLoading,
    isError: detailError,
  } = useQuery({
    queryKey: ['admin-cancel-detail', detailId],
    queryFn: () => adminApi.getCancel(detailId!),
    enabled: detailId !== null,
  });
  const detail = detailWrap?.data;

  const approveMutation = useMutation({
    mutationFn: (id: number) => adminApi.approveCancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
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
      queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
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

  if (isError) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-red-500">{t('admin.error_load')}</div>
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
            {cancels.length === 0 && (
              <tr>
                <td colSpan={7} className="px-6 py-12 text-center text-sm text-gray-500">
                  {t('admin.empty_cancels')}
                </td>
              </tr>
            )}
            {cancels.map((cancel: Cancel) => {
              const st = normalizeCancelStatus(cancel.status);
              return (
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
                <td className="px-6 py-4 text-sm text-gray-900 max-w-md">
                  <p className="font-medium text-gray-900">
                    {cancel.cancelReasonDescription || cancel.cancelReason}
                  </p>
                  {cancel.cancelDetail ? (
                    <p className="mt-1 text-gray-600 text-xs whitespace-pre-wrap wrap-break-word line-clamp-3">
                      {cancel.cancelDetail}
                    </p>
                  ) : null}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      st === 'APPROVED'
                        ? 'bg-green-100 text-green-800'
                        : st === 'REJECTED'
                        ? 'bg-red-100 text-red-800'
                        : st === 'COMPLETED'
                        ? 'bg-blue-100 text-blue-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {st || cancel.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(cancel.createdAt).toLocaleDateString(dateLocale)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center space-x-2">
                    <button
                      type="button"
                      onClick={() => setDetailId(cancel.id)}
                      className="p-2 text-gray-600 hover:bg-gray-100 rounded"
                      title={t('admin.cancel_detail')}
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                    {st === 'REQUESTED' && (
                      <>
                        <button
                          type="button"
                          onClick={() => handleApprove(cancel.id)}
                          className="p-2 text-green-600 hover:bg-green-50 rounded"
                          title={t('admin.approve_title')}
                        >
                          <Check className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => setRejectingId(cancel.id)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded"
                          title={t('admin.reject_title')}
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            );
            })}
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

      {/* Detail modal */}
      {detailId !== null && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={() => setDetailId(null)}
        >
          <div
            className="bg-white rounded-lg p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto shadow-xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">{t('admin.cancel_detail_title')}</h3>
              <button type="button" onClick={() => setDetailId(null)} aria-label={t('admin.cancel_detail_close')}>
                <X className="w-5 h-5" />
              </button>
            </div>
            {detailLoading && (
              <p className="text-sm text-gray-500 py-8 text-center">{t('admin.cancel_detail_loading')}</p>
            )}
            {detailError && (
              <p className="text-sm text-red-500 py-8 text-center">{t('admin.cancel_detail_error')}</p>
            )}
            {!detailLoading && !detailError && detail && (
              <div className="space-y-4 text-sm">
                <div className="grid grid-cols-2 gap-2 text-gray-600">
                  <span>{t('admin.cancel_id_col')}</span>
                  <span className="font-medium text-gray-900">#{detail.id}</span>
                  {detail.cancelNumber ? (
                    <>
                      <span>{t('admin.cancel_number')}</span>
                      <span className="font-mono text-gray-900">{detail.cancelNumber}</span>
                    </>
                  ) : null}
                  <span>{t('admin.order_id')}</span>
                  <span className="font-medium text-gray-900">#{detail.orderId}</span>
                  <span>{t('admin.user_id')}</span>
                  <span className="font-medium text-gray-900">{detail.userId}</span>
                  <span>{t('admin.status')}</span>
                  <span className="font-medium text-gray-900">
                    {detail.statusDescription || detail.status}
                  </span>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500 uppercase mb-1">{t('admin.cancel_reason')}</p>
                  <p className="text-gray-900">{detail.cancelReasonDescription || detail.cancelReason}</p>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500 uppercase mb-1">{t('admin.cancel_detail_text')}</p>
                  <p className="text-gray-900 whitespace-pre-wrap wrap-break-word">
                    {detail.cancelDetail?.trim() ? detail.cancelDetail : t('admin.cancel_detail_empty')}
                  </p>
                </div>
                {detail.rejectedReason ? (
                  <div>
                    <p className="text-xs font-medium text-gray-500 uppercase mb-1">{t('admin.reject_modal_title')}</p>
                    <p className="text-gray-900 whitespace-pre-wrap">{detail.rejectedReason}</p>
                  </div>
                ) : null}
                <div>
                  <p className="text-xs font-medium text-gray-500 uppercase mb-1">{t('admin.cancel_items_title')}</p>
                  {detail.items && detail.items.length > 0 ? (
                    <ul className="border border-gray-200 rounded-lg divide-y divide-gray-100">
                      {detail.items.map((item) => (
                        <li key={item.id} className="px-3 py-2 flex justify-between gap-2">
                          <span className="text-gray-900">{item.productName}</span>
                          <span className="text-gray-600 shrink-0">
                            ×{item.quantity} /{' '}
                            {typeof item.unitPrice === 'number'
                              ? item.unitPrice.toLocaleString(dateLocale)
                              : Number(item.unitPrice).toLocaleString(dateLocale)}
                          </span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-gray-500">{t('admin.cancel_no_items')}</p>
                  )}
                </div>
              </div>
            )}
            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setDetailId(null)}
                className="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800"
              >
                {t('admin.cancel_detail_close')}
              </button>
            </div>
          </div>
        </div>
      )}

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

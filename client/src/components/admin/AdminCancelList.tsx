import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi, Cancel } from '../../api/services/admin';
import { ChevronLeft, ChevronRight, Check, X } from 'lucide-react';

const AdminCancelList = () => {
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
      alert('취소가 승인되었습니다.');
    },
    onError: () => {
      alert('승인에 실패했습니다.');
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      adminApi.rejectCancel(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      alert('취소가 거부되었습니다.');
      setRejectingId(null);
      setRejectReason('');
    },
    onError: () => {
      alert('거부에 실패했습니다.');
    },
  });

  const handleApprove = (id: number) => {
    if (window.confirm('취소를 승인하시겠습니까?')) {
      approveMutation.mutate(id);
    }
  };

  const handleReject = (id: number) => {
    if (!rejectReason.trim()) {
      alert('거부 사유를 입력해주세요.');
      return;
    }
    rejectMutation.mutate({ id, reason: rejectReason });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  const cancels = data?.data.content || [];
  const pageData = data?.data;

  const statusOptions = ['REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED'];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">취소 관리</h2>
          <p className="text-gray-600 mt-1">
            전체 {pageData?.totalElements || 0}건
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
          <option value="">전체 상태</option>
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
                취소 ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                주문 ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                사용자 ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                취소 사유
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                상태
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                요청일
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                작업
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
                  {new Date(cancel.requestedAt).toLocaleDateString('ko-KR')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {cancel.status === 'REQUESTED' && (
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleApprove(cancel.id)}
                        className="p-2 text-green-600 hover:bg-green-50 rounded"
                        title="승인"
                      >
                        <Check className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => setRejectingId(cancel.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded"
                        title="거부"
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
          이전
        </button>
        <span className="text-sm text-gray-700">
          {page + 1} / {pageData?.totalPages || 1}
        </span>
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={pageData?.last}
          className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          다음
          <ChevronRight className="w-4 h-4 ml-1" />
        </button>
      </div>

      {/* Reject Modal */}
      {rejectingId && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">취소 거부 사유</h3>
              <button onClick={() => setRejectingId(null)}>
                <X className="w-5 h-5" />
              </button>
            </div>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="거부 사유를 입력하세요"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg h-32 resize-none"
            />
            <div className="flex space-x-3 mt-4">
              <button
                onClick={() => setRejectingId(null)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={() => handleReject(rejectingId)}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                거부
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminCancelList;

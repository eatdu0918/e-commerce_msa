import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi, Payment } from '../../api/services/admin';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const AdminPaymentList = () => {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-payments', page, statusFilter],
    queryFn: () => adminApi.getPayments(page, 10, statusFilter || undefined),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      adminApi.updatePaymentStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
      alert('결제 상태가 변경되었습니다.');
    },
    onError: () => {
      alert('상태 변경에 실패했습니다.');
    },
  });

  const handleStatusChange = (paymentId: number, newStatus: string) => {
    if (window.confirm('결제 상태를 변경하시겠습니까?')) {
      updateStatusMutation.mutate({ id: paymentId, status: newStatus });
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  const payments = data?.data?.content || [];
  const pageData = data?.data;

  const statusOptions = ['PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">결제 관리</h2>
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
                결제 ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                주문 ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                결제 금액
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                결제 방법
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                상태
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                결제일
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                수정일
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {payments.map((payment: Payment) => (
              <tr key={payment.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  #{payment.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  #{payment.orderId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {payment.amount.toLocaleString()}원
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {payment.paymentMethod}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <select
                    value={payment.status}
                    onChange={(e) => handleStatusChange(payment.id, e.target.value)}
                    className="text-sm border border-gray-300 rounded px-2 py-1"
                  >
                    {statusOptions.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(payment.createdAt).toLocaleDateString('ko-KR')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(payment.updatedAt).toLocaleDateString('ko-KR')}
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
    </div>
  );
};

export default AdminPaymentList;

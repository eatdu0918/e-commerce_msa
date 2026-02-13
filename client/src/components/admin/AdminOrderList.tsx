import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMyOrders } from '../../api/services/order'; // Using getMyOrders as proxy for now since true Admin API might be different
import { Eye, Search, Filter } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function AdminOrderList() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);

  const { data: orderData, isLoading } = useQuery({
    queryKey: ['admin-orders', page],
    queryFn: getMyOrders, // Ideally allow pagination param
  });

  const orders = orderData || [];

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold">Orders</h1>
          <p className="text-stone-500 text-sm">Managing client orders</p>
        </div>
      </div>

      <div className="bg-white rounded-[20px] shadow-sm border border-stone-100 overflow-hidden">
        <div className="p-5 border-b border-stone-100 flex items-center justify-between bg-stone-50/50">
          <div className="flex space-x-2">
            <div className="relative">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" />
              <input
                type="text"
                placeholder="Order #, Customer..."
                className="pl-9 pr-4 py-2 bg-white border border-stone-200 rounded-lg text-sm focus:ring-1 focus:ring-black focus:border-black outline-none w-64 transition-all"
              />
            </div>
            <button className="flex items-center px-3 py-2 bg-white border border-stone-200 rounded-lg text-sm text-stone-600 hover:bg-stone-50 transition-colors">
              <Filter size={16} className="mr-2" />
              Filter
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="bg-stone-50 text-stone-500 font-medium border-b border-stone-100">
              <tr>
                <th className="px-6 py-4">Order ID</th>
                <th className="px-6 py-4">Date</th>
                <th className="px-6 py-4">Customer</th>
                <th className="px-6 py-4">Total</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-50">
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={6} className="px-6 py-4">
                      <div className="h-10 bg-stone-100 rounded-lg" />
                    </td>
                  </tr>
                ))
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-stone-400">
                    No orders found
                  </td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr key={order.id} className="hover:bg-stone-50/50 transition-colors relative group">
                    <td className="px-6 py-4 font-medium">#{order.id}</td>
                    <td className="px-6 py-4 text-stone-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div className="w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-[10px] font-bold mr-2">
                          U
                        </div>
                        <span>User #{order.userId}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 font-bold">₩{order.totalAmount.toLocaleString()}</td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${order.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                          order.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                            'bg-blue-100 text-blue-800'
                        }`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => navigate(`/checkout/complete/${order.id}`)} // Re-using existing detail view for demo
                        className="p-2 text-stone-400 hover:text-black hover:bg-stone-100 rounded-lg transition-colors"
                      >
                        <Eye size={16} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

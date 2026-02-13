import { Users, ShoppingCart, CreditCard, Ticket } from 'lucide-react';

const AdminDashboard = () => {
  const stats = [
    {
      title: '전체 사용자',
      value: '1,234',
      icon: Users,
      bgColor: 'bg-blue-100',
      iconColor: 'text-blue-600',
    },
    {
      title: '총 주문',
      value: '567',
      icon: ShoppingCart,
      bgColor: 'bg-green-100',
      iconColor: 'text-green-600',
    },
    {
      title: '결제 완료',
      value: '489',
      icon: CreditCard,
      bgColor: 'bg-purple-100',
      iconColor: 'text-purple-600',
    },
    {
      title: '활성 쿠폰',
      value: '23',
      icon: Ticket,
      bgColor: 'bg-orange-100',
      iconColor: 'text-orange-600',
    },
  ];

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900 mb-6">대시보드</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="bg-white rounded-lg shadow p-6 border border-gray-200"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
                </div>
                <div className={`${stat.bgColor} p-3 rounded-lg`}>
                  <Icon className={`w-6 h-6 ${stat.iconColor}`} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-8 bg-white rounded-lg shadow p-6 border border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">최근 활동</h3>
        <div className="space-y-4">
          <div className="flex items-center justify-between py-3 border-b border-gray-100">
            <div>
              <p className="font-medium text-gray-900">새로운 주문</p>
              <p className="text-sm text-gray-500">주문 #1234가 생성되었습니다.</p>
            </div>
            <span className="text-sm text-gray-500">5분 전</span>
          </div>
          <div className="flex items-center justify-between py-3 border-b border-gray-100">
            <div>
              <p className="font-medium text-gray-900">사용자 가입</p>
              <p className="text-sm text-gray-500">user@example.com이 가입했습니다.</p>
            </div>
            <span className="text-sm text-gray-500">12분 전</span>
          </div>
          <div className="flex items-center justify-between py-3">
            <div>
              <p className="font-medium text-gray-900">쿠폰 생성</p>
              <p className="text-sm text-gray-500">WELCOME2024 쿠폰이 생성되었습니다.</p>
            </div>
            <span className="text-sm text-gray-500">1시간 전</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;

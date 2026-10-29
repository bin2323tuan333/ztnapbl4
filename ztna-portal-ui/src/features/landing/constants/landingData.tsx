import { Shield, Lock, Eye } from 'lucide-react';
import type { Feature } from '../types';

export const LANDING_DATA: Feature[] = [
    {
        id: 1,
        icon: <Shield size={28} />,
        title: 'Zero Trust Security',
        description: 'Xác thực mọi yêu cầu truy cập, không tin tưởng mặc định bất kỳ kết nối nào.',
    },
    {
        id: 2,
        icon: <Lock size={28} />,
        title: 'Kiểm soát truy cập',
        description: 'Quản lý quyền truy cập chi tiết theo người dùng, tài nguyên và thời gian.',
    },
    {
        id: 3,
        icon: <Eye size={28} />,
        title: 'Giám sát toàn diện',
        description: 'Theo dõi và ghi nhật ký mọi hoạt động truy cập trong thời gian thực.',
    },
];
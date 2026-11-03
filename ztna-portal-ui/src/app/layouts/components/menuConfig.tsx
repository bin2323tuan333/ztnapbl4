import type { ReactNode } from 'react';
import {
    LayoutDashboard,
    Users,
    Package,
    KeyRound,
    ScrollText,
    Settings,
} from 'lucide-react';

export type MenuItem = {
    label: string;
    path?: string;
    icon?: ReactNode;
    children?: MenuItem[];
};

export const ADMIN_MENU: MenuItem[] = [
    {
        label: 'Tổng quan',
        icon: <LayoutDashboard size={20} />,
        children: [
            { label: 'Bảng điều khiển', path: '/dashboard' },
            { label: 'Báo cáo & Thống kê', path: '/reports' },
        ],
    },

    {
        label: 'Quản lý danh tính',
        icon: <Users size={20} />,
        children: [
            { label: 'Quản trị viên', path: '/users?role=ADMIN' },
            { label: 'Người dùng thường', path: '/users?role=USER' },
        ],
    },

    {
        label: 'Quản lý tài nguyên',
        icon: <Package size={20} />,
        children: [
            { label: 'Ứng dụng', path: '/resources' },
            { label: 'Connectors', path: '/connectors' },
        ],
    },

    {
        label: 'Phân quyền',
        icon: <KeyRound size={20} />,
        children: [
            { label: 'Quản lý quyền', path: '/rules' },
        ],
    },

    {
        label: 'Nhật ký & Kiểm toán',
        icon: <ScrollText size={20} />,
        children: [
            { label: 'Log truy cập', path: '/network-logs' },
            { label: 'Log kiểm toán', path: '/audit-logs' },
        ],
    },
];

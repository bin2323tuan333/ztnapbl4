import type { ReactNode } from 'react';
import {
    BarChart2,
    Users,
    Package,
    KeyRound,
    ScrollText,
} from 'lucide-react';

// ============================================================
// TYPE — chỉ 1 bậc sub
// ============================================================

export type MenuItem = {
    label: string;
    /** Có path → click điều hướng; không có → click toggle dropdown */
    path?: string;
    icon?: ReactNode;
    /** Chỉ tồn tại ở cấp 1, children là link đơn (không có children) */
    children?: MenuItem[];
};

// ============================================================
// MENU ADMIN — mỗi item lớn có dropdown 1 bậc
// ============================================================

export const ADMIN_MENU: MenuItem[] = [
    // ---------- TỔNG QUAN ----------
    {
        label: 'Tổng quan',
        icon: <BarChart2 size={20} />,
        children: [
            { label: 'Insights', path: '/app/home' },
            { label: 'Thống kê truy cập', path: '/app/stats/access' },
            { label: 'Thống kê quyền', path: '/app/stats/permissions' },
        ],
    },

    // ---------- QUẢN LÝ DANH TÍNH ----------
    {
        label: 'Quản lý danh tính',
        icon: <Users size={20} />,
        children: [
            { label: 'Người dùng', path: '/app/users' },
            { label: 'Quản trị viên', path: '/app/users?role=ADMIN' },
            { label: 'Người giám sát', path: '/app/users?role=SUPERVISOR' },
            { label: 'Người dùng đích', path: '/app/users?role=TARGET_USER' },
            { label: 'Vai trò', path: '/app/roles' },
            { label: 'Chức vụ', path: '/app/positions' },
        ],
    },

    // ---------- QUẢN LÝ TÀI NGUYÊN ----------
    {
        label: 'Quản lý tài nguyên',
        icon: <Package size={20} />,
        children: [
            { label: 'Ứng dụng', path: '/app/resources' },
            { label: 'Connectors', path: '/app/connectors' },
        ],
    },

    // ---------- PHÂN QUYỀN ----------
    {
        label: 'Phân quyền',
        icon: <KeyRound size={20} />,
        children: [
            { label: 'Tất cả quyền', path: '/app/permissions' },
            { label: 'Quyền theo vai trò', path: '/app/permissions?scope=role' },
            { label: 'Quyền theo chức vụ', path: '/app/permissions?scope=position' },
            { label: 'Quyền riêng user', path: '/app/permissions?scope=user' },
            { label: 'Quyền sắp hết hạn', path: '/app/permissions/expiring' },
        ],
    },

    // ---------- NHẬT KÝ ----------
    {
        label: 'Nhật ký & Kiểm toán',
        icon: <ScrollText size={20} />,
        children: [
            { label: 'Log truy cập', path: '/app/logs/access' },
            { label: 'Log kiểm toán', path: '/app/logs/audit' },
        ],
    },
];
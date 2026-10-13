// ============================================================
// UNION TYPES (thay cho enum — dễ serialize qua localStorage)
// ============================================================

export type SystemRoleCode = 'ADMIN' | 'SUPERVISOR' | 'TARGET_USER';
export type ConnectorStatus = 'online' | 'offline' | 'degraded';
export type PermissionEffect = 'ALLOW' | 'DENY';
export type PermissionSource = 'ROLE' | 'POSITION' | 'USER';
export type AccessDecision = 'allow' | 'deny';
export type AccessStatus = 'success' | 'failed';

// ============================================================
// NGƯỜI DÙNG & PHÂN LOẠI
// ============================================================

export type SystemRole = {
    id: number;
    code: SystemRoleCode;
    name: string;
    display_name: string;
    description: string;
    /** true với ADMIN — hệ thống chỉ cho phép 1 user giữ role này */
    is_unique: boolean;
    created_at: string;
};

export type Position = {
    id: number;
    code: string;
    name: string;
    display_name: string;
    description: string;
    /** Số càng lớn càng cao — dùng để sort, không dùng để phân quyền */
    level: number;
    enabled: boolean;
    created_at: string;
};

export type User = {
    id: number;
    username: string;
    email: string;
    full_name: string;
    enabled: boolean;

    // Liên kết phân loại — luôn có role, position có thể null
    role_id: number;
    role: SystemRole;
    position_id: number | null;
    position: Position | null;

    last_login_at: string | null;
    created_at: string;
    updated_at: string;
};

// ============================================================
// TÀI NGUYÊN
// ============================================================

export type Connector = {
    id: number;
    code: string;
    name: string;
    connector_zone: string;
    /** 'http' | 'grpc' | 'tcp' | 'ssh' ... */
    type: string;
    status: ConnectorStatus;
    last_seen: string;
    created_at: string;
};

export type Resource = {
    id: number;
    code: string;
    name: string;
    description: string;
    backend_url: string;
    connector_id: number;
    connector_zone: string;
    enabled: boolean;
    created_at: string;
};

// ============================================================
// CẤP QUYỀN — 3 TẦNG
// ============================================================

/** Quyền theo vai trò — admin gán 1 lần, áp cho mọi user cùng role */
export type RolePermission = {
    id: number;
    role_id: number;
    role_code: SystemRoleCode;
    resource_id: number;
    resource_name: string;
    effect: PermissionEffect;
    created_at: string;
};

/** Quyền theo chức vụ — áp cho mọi user cùng position */
export type PositionPermission = {
    id: number;
    position_id: number;
    position_code: string;
    resource_id: number;
    resource_name: string;
    effect: PermissionEffect;
    created_at: string;
};

/**
 * Quyền riêng của user — "cấp quyền ngoài chức vụ".
 * Đây là phần bạn mô tả: chọn app cụ thể, không phụ thuộc role/position.
 */
export type UserPermission = {
    id: number;
    user_id: number;
    username: string;
    resource_id: number;
    resource_name: string;
    effect: PermissionEffect;
    granted_by: string;
    granted_at: string;
    expires_at: string | null;
    reason: string;
    enabled: boolean;
};

/** Kết quả tổng hợp — dùng cho UI hiển thị "quyền hiệu lực" của 1 user */
export type EffectivePermission = {
    resource_id: number;
    resource_name: string;
    effect: PermissionEffect;
    sources: PermissionSource[];
};

// ============================================================
// LOG
// ============================================================

export type AccessLog = {
    id: number;
    request_id: string;
    user_id: number;
    username: string;
    resource_name: string;
    path: string;
    method: string;
    client_ip: string;
    destination_port: number;
    protocol: string;
    decision: AccessDecision;
    reason: string;
    created_at: string;

    // Bổ sung — optional để tương thích dữ liệu cũ
    device_name?: string;
    source?: string;
    destination?: string;
    status?: AccessStatus;
    latency?: number;
    connector_zone?: string;
};

export type AuditLog = {
    id: number;
    actor_id: number;
    actor_username: string;
    action: string;
    target_type: string;
    target_id: number;
    details: string;
    ip_address: string;
    created_at: string;
};

// ============================================================
// DASHBOARD
// ============================================================

export type DashboardSummary = {
    total_users: number;
    active_users: number;
    total_resources: number;
    total_connectors: number;
    online_connectors: number;
    total_permissions: number;
    expiring_permissions: number;
    access_today: number;
    deny_today: number;
};

// ============================================================
// TABLE
// ============================================================

import type { ReactNode } from 'react';

export type Column<T> = {
    key: string;
    header: string;
    width?: string;
    render?: (item: T) => ReactNode;
};
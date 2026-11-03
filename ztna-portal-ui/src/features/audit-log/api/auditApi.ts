import { getRequest } from '@/shared/utils/apiClient';

/** Khớp Action phía backend — dùng type, KHÔNG dùng enum */
export type AuditAction =
    | 'LOGIN'
    | 'LOGIN_FAILED'
    | 'LOGOUT'
    | 'REFRESH_TOKEN'
    | 'CREATE_USER'
    | 'UPDATE_USER'
    | 'BLOCK_USER'
    | 'UNBLOCK_USER'
    | 'RESET_PASSWORD'
    | 'DELETE_USER'
    | 'CREATE_RESOURCE'
    | 'UPDATE_RESOURCE'
    | 'DELETE_RESOURCE'
    | 'ENABLE_RESOURCE'
    | 'DISABLE_RESOURCE'
    | 'CREATE_CONNECTOR'
    | 'UPDATE_CONNECTOR'
    | 'DELETE_CONNECTOR'
    | 'ENABLE_CONNECTOR'
    | 'DISABLE_CONNECTOR'
    | 'CONNECTOR_ONLINE'
    | 'CONNECTOR_OFFLINE'
    | 'CREATE_RULE'
    | 'UPDATE_RULE'
    | 'DELETE_RULE'
    | 'ENABLE_RULE'
    | 'DISABLE_RULE'
    | 'ASSIGN_USER_TO_RULE'
    | 'ASSIGN_RESOURCE_TO_RULE';

/** Khớp TargetType phía backend */
export type TargetType =
    | 'USER'
    | 'RESOURCE'
    | 'PERMISSION'
    | 'CONNECTOR'
    | 'SESSION'
    | 'SYSTEM';

export interface AuditLog {
    id: string;
    actorId: string | null;
    actorUsername: string;
    action: string;
    targetType: TargetType | string | null;
    targetId: string | null;
    details: string;
    ipAddress: string | null;
    createdAt: string;
}

export interface AuditSearchParams {
    actorUsername?: string;
    action?: AuditAction | '';
    targetType?: TargetType | '';
    from?: string;
    to?: string;
    page?: number;
    size?: number;
    sort?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

const BASE = '/api/v1/admin/audit-logs';

export const ACTION_OPTIONS: { value: AuditAction; label: string }[] = [
    { value: 'LOGIN', label: 'Đăng nhập' },
    { value: 'LOGIN_FAILED', label: 'Đăng nhập thất bại' },
    { value: 'LOGOUT', label: 'Đăng xuất' },
    { value: 'REFRESH_TOKEN', label: 'Làm mới token' },
    { value: 'CREATE_USER', label: 'Tạo user' },
    { value: 'UPDATE_USER', label: 'Cập nhật user' },
    { value: 'BLOCK_USER', label: 'Khóa user' },
    { value: 'UNBLOCK_USER', label: 'Mở khóa user' },
    { value: 'RESET_PASSWORD', label: 'Đặt lại mật khẩu' },
    { value: 'DELETE_USER', label: 'Xóa user' },
    { value: 'CREATE_RESOURCE', label: 'Tạo resource' },
    { value: 'UPDATE_RESOURCE', label: 'Cập nhật resource' },
    { value: 'DELETE_RESOURCE', label: 'Xóa resource' },
    { value: 'ENABLE_RESOURCE', label: 'Kích hoạt resource' },
    { value: 'DISABLE_RESOURCE', label: 'Vô hiệu hóa resource' },
    { value: 'CREATE_CONNECTOR', label: 'Tạo connector' },
    { value: 'UPDATE_CONNECTOR', label: 'Cập nhật connector' },
    { value: 'DELETE_CONNECTOR', label: 'Xóa connector' },
    { value: 'ENABLE_CONNECTOR', label: 'Kích hoạt connector' },
    { value: 'DISABLE_CONNECTOR', label: 'Vô hiệu hóa connector' },
    { value: 'CONNECTOR_ONLINE', label: 'Connector online' },
    { value: 'CONNECTOR_OFFLINE', label: 'Connector offline' },
    { value: 'CREATE_RULE', label: 'Tạo rule' },
    { value: 'UPDATE_RULE', label: 'Cập nhật rule' },
    { value: 'DELETE_RULE', label: 'Xóa rule' },
    { value: 'ENABLE_RULE', label: 'Kích hoạt rule' },
    { value: 'DISABLE_RULE', label: 'Vô hiệu hóa rule' },
    { value: 'ASSIGN_USER_TO_RULE', label: 'Gán user vào rule' },
    { value: 'ASSIGN_RESOURCE_TO_RULE', label: 'Gán resource vào rule' },
];

export const TARGET_TYPE_OPTIONS: { value: TargetType; label: string }[] = [
    { value: 'USER', label: 'User' },
    { value: 'RESOURCE', label: 'Resource' },
    { value: 'PERMISSION', label: 'Permission' },
    { value: 'CONNECTOR', label: 'Connector' },
    { value: 'SESSION', label: 'Session' },
    { value: 'SYSTEM', label: 'System' },
];

export function actionBadgeClass(action: string): string {
    if (
        action.includes('FAILED') ||
        action.includes('DELETE') ||
        action.includes('BLOCK') ||
        action.includes('DISABLE') ||
        action.includes('OFFLINE')
    ) {
        return 'red';
    }
    if (
        action.includes('CREATE') ||
        action.includes('ENABLE') ||
        action.includes('ONLINE') ||
        action === 'LOGIN'
    ) {
        return 'green';
    }
    if (
        action.includes('UPDATE') ||
        action.includes('ASSIGN') ||
        action.includes('RESET') ||
        action.includes('UNBLOCK')
    ) {
        return 'blue';
    }
    return 'gray';
}

function buildQuery(params: AuditSearchParams): string {
    const q = new URLSearchParams();
    if (params.actorUsername?.trim()) q.set('actorUsername', params.actorUsername.trim());
    if (params.action) q.set('action', params.action);
    if (params.targetType) q.set('targetType', params.targetType);
    if (params.from) q.set('from', params.from);
    if (params.to) q.set('to', params.to);
    q.set('page', String(params.page ?? 0));
    q.set('size', String(params.size ?? 20));
    q.set('sort', params.sort ?? 'createdAt,desc');
    return q.toString();
}

export const auditApi = {
    search: (params: AuditSearchParams = {}): Promise<PageResponse<AuditLog>> => {
        return getRequest(`${BASE}?${buildQuery(params)}`);
    },
};
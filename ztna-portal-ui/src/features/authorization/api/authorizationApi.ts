import { getRequest, postRequest, deleteRequest } from '@/shared/utils/apiClient';

// ───────────── Kiểu dữ liệu ─────────────

export type PermissionType = 'ROLE' | 'USER';
export type PermissionAction = 'READ' | 'WRITE' | 'DELETE' | 'EXECUTE';

// Khớp với PermissionResponse
export interface PermissionItem {
    id: string;
    type: PermissionType;
    roleId: string | null;
    roleName: string | null;
    userId: string | null;
    username: string | null;
    resourceId: string;
    resourceName: string;
    action: PermissionAction | null;
    grantedBy: string;
    grantedAt: string;
    expiresAt: string | null;
    reason: string;
    enabled: boolean;
    active: boolean;   // true = đang có hiệu lực (enabled và chưa hết hạn)
}

export interface GrantRoleBody {
    roleId: string;
    resourceId: string;
    action: PermissionAction;
    reason: string;
    expiresAt: string | null;   // ISO, null = không hết hạn
}

export interface GrantUserBody {
    userId: string;
    resourceId: string;
    reason: string;
    expiresAt: string | null;
}

// Khớp với EffectiveAccessResponse (dùng ở bước sau)
export interface EffectiveAccess {
    resourceId: string;
    resourceName: string;
    sources: string[];
}

// Dữ liệu cho các ô chọn trong form (chỉ lấy vài trường cần dùng)
export interface RoleOption { id: string; name: string; displayName: string; }
export interface UserOption { id: string; username: string; fullName: string; }
export interface ResourceOption { id: string; name: string; type: string; enabled: boolean; }

// ───────────── API ─────────────

const PERM = '/api/v1/admin/permissions';

export const permissionApi = {
    getRolePermissions: (): Promise<PermissionItem[]> => getRequest(`${PERM}/roles`),
    getUserPermissions: (): Promise<PermissionItem[]> => getRequest(`${PERM}/users`),
    grantToRole: (body: GrantRoleBody): Promise<PermissionItem> => postRequest(`${PERM}/roles`, body),
    grantToUser: (body: GrantUserBody): Promise<PermissionItem> => postRequest(`${PERM}/users`, body),
    revokeFromRole: (id: string) => deleteRequest(`${PERM}/roles/${id}`),
    revokeFromUser: (id: string) => deleteRequest(`${PERM}/users/${id}`),
    getEffective: (userId: string): Promise<EffectiveAccess[]> => getRequest(`${PERM}/effective/${userId}`),
};

export const optionApi = {
    getRoles: (): Promise<RoleOption[]> => getRequest('/api/v1/admin/roles'),
    getUsers: (): Promise<UserOption[]> => getRequest('/api/v1/admin/users'),
    getResources: (): Promise<ResourceOption[]> => getRequest('/api/v1/admin/resources'),
};

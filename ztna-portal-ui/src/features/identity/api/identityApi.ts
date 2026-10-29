import { getRequest, postRequest, putRequest, patchRequest, deleteRequest } from '@/shared/utils/apiClient';
import type { User, UserRole } from '@/shared/types/model/User';

// ───────────── Kiểu dữ liệu ─────────────

export interface CreateUserBody {
    username: string;
    fullName: string;
    email: string;
    password: string;
    role: UserRole;
}

export interface UpdateUserBody {
    fullName: string;
    email: string;
    role: UserRole;
}

// Khớp với SystemRoleResponse
export interface RoleItem {
    id: string;
    name: string;          // mã role, vd DEV
    displayName: string;   // tên hiển thị, vd "Lập trình viên"
    description: string;
    memberCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface RoleBody {
    name: string;
    displayName: string;
    description: string;
}

// Khớp với RoleMemberResponse
export interface RoleMember {
    userId: string;
    username: string;
    fullName: string;
    roleId: string;
    roleName: string;
    grantedBy: string;
    grantedAt: string;
    expiresAt: string | null;
    reason: string;
    enabled: boolean;
}

export interface AssignRoleBody {
    userId: string;
    reason: string;
    expiresAt: string | null;   // ISO, null = không hết hạn
}

// ───────────── API ─────────────

const USERS = '/api/v1/admin/users';
const ROLES = '/api/v1/admin/roles';

export const userApi = {
    getAll: (): Promise<User[]> => getRequest(USERS),
    create: (body: CreateUserBody): Promise<User> => postRequest(USERS, body),
    update: (id: string, body: UpdateUserBody): Promise<User> => putRequest(`${USERS}/${id}`, body),
    resetPassword: (id: string, password: string) => putRequest(`${USERS}/${id}/password`, { password }),
    block: (id: string) => patchRequest(`${USERS}/${id}/block`),
    unblock: (id: string) => patchRequest(`${USERS}/${id}/unblock`),
    remove: (id: string) => deleteRequest(`${USERS}/${id}`),
};

export const roleApi = {
    getAll: (): Promise<RoleItem[]> => getRequest(ROLES),
    create: (body: RoleBody): Promise<RoleItem> => postRequest(ROLES, body),
    update: (id: string, body: RoleBody): Promise<RoleItem> => putRequest(`${ROLES}/${id}`, body),
    remove: (id: string) => deleteRequest(`${ROLES}/${id}`),
    getMembers: (id: string): Promise<RoleMember[]> => getRequest(`${ROLES}/${id}/users`),
    assign: (id: string, body: AssignRoleBody) => postRequest(`${ROLES}/${id}/users`, body),
    unassign: (id: string, userId: string) => deleteRequest(`${ROLES}/${id}/users/${userId}`),
};

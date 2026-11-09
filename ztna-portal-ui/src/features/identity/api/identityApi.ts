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

// ───────────── API ─────────────

const USERS = '/api/v1/admin/users';

export const userApi = {
    getAll: (): Promise<User[]> => getRequest(USERS),
    create: (body: CreateUserBody): Promise<User> => postRequest(USERS, body),
    update: (id: string, body: UpdateUserBody): Promise<User> => putRequest(`${USERS}/${id}`, body),
    resetPassword: (id: string, password: string) => putRequest(`${USERS}/${id}/password`, { password }),
    block: (id: string) => patchRequest(`${USERS}/${id}/block`),
    unblock: (id: string) => patchRequest(`${USERS}/${id}/unblock`),
    remove: (id: string) => deleteRequest(`${USERS}/${id}`),
};

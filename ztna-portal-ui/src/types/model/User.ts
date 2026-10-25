// Khớp với UserResponse của backend (GET /admin/users)
export type UserRole = 'ADMIN' | 'SUPERVISOR' | 'USER';

export interface AdminUser {
    id: string;
    username: string;
    fullName: string;
    email: string | null;
    role: UserRole;
    enabled: boolean;
    lastLoginAt: string | null;
    createdAt: string;
}

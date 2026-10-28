// Khớp với SystemRoleResponse (GET /admin/roles)
export interface RoleItem {
    id: string;
    name: string;          // mã role, vd DEV
    displayName: string;   // tên hiển thị, vd "Lập trình viên"
    description: string;
    memberCount: number;
    createdAt: string;
    updatedAt: string;
}

// Khớp với RoleMemberResponse (GET /admin/roles/{id}/users)
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

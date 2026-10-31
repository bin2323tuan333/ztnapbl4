import { useState, useEffect, useCallback, useMemo } from 'react';
import {
    permissionApi, optionApi,
    type PermissionItem, type GrantRoleBody, type GrantUserBody,
    type RoleOption, type UserOption, type ResourceOption,
} from '../api/authorizationApi';

// Phạm vi hiển thị, ứng với từng mục trên menu Phân quyền
export type PermissionScope = 'all' | 'role' | 'user' | 'resource' | 'expiring';

const SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;

export const usePermissions = (scope: PermissionScope) => {
    const [perms, setPerms] = useState<PermissionItem[]>([]);
    const [roles, setRoles] = useState<RoleOption[]>([]);
    const [users, setUsers] = useState<UserOption[]>([]);
    const [resources, setResources] = useState<ResourceOption[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [resourceFilter, setResourceFilter] = useState('');   // dùng khi scope = 'resource'

    // Quyền theo role + quyền riêng user, gộp chung một bảng
    const fetchPerms = useCallback(async () => {
        const [rolePerms, userPerms] = await Promise.all([
            permissionApi.getRolePermissions(),
            permissionApi.getUserPermissions(),
        ]);
        setPerms([...rolePerms, ...userPerms]);
    }, []);

    const fetchAll = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            const [, r, u, res] = await Promise.all([
                fetchPerms(),
                optionApi.getRoles(),
                optionApi.getUsers(),
                optionApi.getResources(),
            ]);
            setRoles(r);
            setUsers(u);
            setResources(res);
        } catch (err: any) {
            setError(err.status === 403 ? 'Bạn không có quyền xem trang này' : (err.message || 'Không tải được dữ liệu phân quyền'));
        } finally {
            setIsLoading(false);
        }
    }, [fetchPerms]);

    useEffect(() => {
        fetchAll();
    }, [fetchAll]);

    const filteredPerms = useMemo(() => {
        const now = Date.now();
        const lower = searchTerm.toLowerCase();
        return perms
            .filter(p => {
                switch (scope) {
                    case 'role': return p.type === 'ROLE';
                    case 'user': return p.type === 'USER';
                    case 'resource': return resourceFilter === '' || p.resourceId === resourceFilter;
                    case 'expiring':
                        return p.active && p.expiresAt !== null
                            && new Date(p.expiresAt).getTime() - now <= SEVEN_DAYS;
                    default: return true;
                }
            })
            .filter(p =>
                (p.roleName ?? '').toLowerCase().includes(lower) ||
                (p.username ?? '').toLowerCase().includes(lower) ||
                p.resourceName.toLowerCase().includes(lower) ||
                (p.reason ?? '').toLowerCase().includes(lower)
            );
    }, [perms, scope, resourceFilter, searchTerm]);

    // Lỗi được ném ra cho trang hiển thị trong modal / alert
    const grantToRole = async (body: GrantRoleBody) => { await permissionApi.grantToRole(body); await fetchPerms(); };
    const grantToUser = async (body: GrantUserBody) => { await permissionApi.grantToUser(body); await fetchPerms(); };
    const revoke = async (p: PermissionItem) => {
        if (p.type === 'ROLE') await permissionApi.revokeFromRole(p.id);
        else await permissionApi.revokeFromUser(p.id);
        await fetchPerms();
    };

    return {
        perms: filteredPerms,
        roles,
        users,
        resources,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        resourceFilter,
        setResourceFilter,
        grantToRole,
        grantToUser,
        revoke,
        refetch: fetchAll,
    };
};

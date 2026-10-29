import { useState, useEffect, useCallback, useMemo } from 'react';
import { roleApi, type RoleItem, type RoleBody, type RoleMember, type AssignRoleBody } from '../api/identityApi';

export const useRoles = () => {
    const [roles, setRoles] = useState<RoleItem[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            setRoles(await roleApi.getAll());
        } catch (err: any) {
            setError(err.message || 'Không tải được danh sách vai trò');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const filteredRoles = useMemo(() => {
        const lower = searchTerm.toLowerCase();
        return roles.filter(r =>
            r.name.toLowerCase().includes(lower) ||
            r.displayName.toLowerCase().includes(lower)
        );
    }, [roles, searchTerm]);

    // Mã role luôn viết hoa, bỏ khoảng trắng hai đầu
    const normalize = (body: RoleBody): RoleBody => ({ ...body, name: body.name.trim().toUpperCase() });

    const saveRole = async (id: string | null, body: RoleBody) => {
        if (id) await roleApi.update(id, normalize(body));
        else await roleApi.create(normalize(body));
        await fetchData();
    };
    const deleteRole = async (id: string) => { await roleApi.remove(id); await fetchData(); };

    // ───────────── Thành viên ─────────────
    const [members, setMembers] = useState<RoleMember[]>([]);

    const loadMembers = async (roleId: string) => setMembers(await roleApi.getMembers(roleId));

    const assignUser = async (roleId: string, body: AssignRoleBody) => {
        await roleApi.assign(roleId, body);
        await Promise.all([loadMembers(roleId), fetchData()]);   // cập nhật cả số thành viên
    };
    const unassignUser = async (roleId: string, userId: string) => {
        await roleApi.unassign(roleId, userId);
        await Promise.all([loadMembers(roleId), fetchData()]);
    };

    return {
        roles: filteredRoles,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        saveRole,
        deleteRole,
        members,
        setMembers,
        loadMembers,
        assignUser,
        unassignUser,
    };
};

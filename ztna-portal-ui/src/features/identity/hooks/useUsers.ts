import { useState, useEffect, useCallback, useMemo } from 'react';
import type { User, UserRole } from '@/shared/types/model/User';
import { userApi, type CreateUserBody, type UpdateUserBody } from '../api/identityApi';

// roleFilter lấy từ ?role= trên menu (null = tất cả)
export const useUsers = (roleFilter: UserRole | null) => {
    const [users, setUsers] = useState<User[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            setUsers(await userApi.getAll());
        } catch (err: any) {
            setError(err.status === 403 ? 'Bạn không có quyền xem trang này' : (err.message || 'Không tải được danh sách'));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const filteredUsers = useMemo(() => {
        const lower = searchTerm.toLowerCase();
        return users
            .filter(u => roleFilter === null || u.role === roleFilter)
            .filter(u =>
                u.username.toLowerCase().includes(lower) ||
                (u.fullName ?? '').toLowerCase().includes(lower) ||
                (u.email ?? '').toLowerCase().includes(lower)
            );
    }, [users, roleFilter, searchTerm]);

    // Các thao tác: gọi API xong tải lại danh sách. Lỗi được ném ra cho trang hiển thị.
    const createUser = async (body: CreateUserBody) => { await userApi.create(body); await fetchData(); };
    const updateUser = async (id: string, body: UpdateUserBody) => { await userApi.update(id, body); await fetchData(); };
    const resetPassword = (id: string, password: string) => userApi.resetPassword(id, password);
    const toggleBlock = async (u: User) => {
        if (u.enabled) await userApi.block(u.id); else await userApi.unblock(u.id);
        await fetchData();
    };
    const deleteUser = async (id: string) => { await userApi.remove(id); await fetchData(); };

    return {
        users: filteredUsers,
        allUsers: users,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        createUser,
        updateUser,
        resetPassword,
        toggleBlock,
        deleteUser,
        refetch: fetchData,
    };
};

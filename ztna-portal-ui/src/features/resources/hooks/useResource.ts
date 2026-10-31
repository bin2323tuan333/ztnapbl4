import { useState, useEffect, useCallback, useMemo } from 'react';
import { resourceApi, type Resource } from '../api/resourceApi';

export const useResources = () => {
    const [apps, setApps] = useState<Resource[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await resourceApi.getApplications();
            setApps(data);
            setError('');
        } catch (err: any) {
            setError(err.message || 'Lỗi tải dữ liệu');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const filteredApps = useMemo(() => {
        if (!searchTerm) return apps;
        const lower = searchTerm.toLowerCase();
        return apps.filter(app =>
            app.name.toLowerCase().includes(lower) ||
            app.host.toLowerCase().includes(lower)
        );
    }, [apps, searchTerm]);

    const toggleStatus = async (id: string, currentStatus: boolean) => {
        setApps(prev => prev.map(app => app.id === id ? { ...app, enabled: !currentStatus } : app));
        try {
            await resourceApi.toggleStatus(id, !currentStatus);
        } catch (err) {
            setApps(prev => prev.map(app => app.id === id ? { ...app, enabled: currentStatus } : app));
            alert('Không thể đổi trạng thái, vui lòng thử lại!');
        }
    };

    const deleteApp = async (id: string) => {
        await resourceApi.deleteResource(id);
        setApps(prev => prev.filter(app => app.id !== id));
    };

    return {
        apps: filteredApps,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        toggleStatus,
        deleteApp,
        refetch: fetchData
    };
};
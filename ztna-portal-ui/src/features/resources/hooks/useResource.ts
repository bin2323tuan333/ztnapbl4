import { useState, useEffect, useCallback, useMemo } from 'react';
import { resourceApi, type Application } from '../api/resourceApi';

export const useResources = () => {
    const [apps, setApps] = useState<Application[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await resourceApi.getApplications();
            setApps(data);
        } catch (err: any) {
            setError(err.message || 'Lỗi tải dữ liệu');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    // Lọc theo tên hoặc IP
    const filteredApps = useMemo(() => {
        if (!searchTerm) return apps;
        const lower = searchTerm.toLowerCase();
        return apps.filter(app =>
            app.name.toLowerCase().includes(lower) ||
            app.domain.includes(lower)
        );
    }, [apps, searchTerm]);

    // Giả lập tính năng bật/tắt (sau này gọi API PATCH)
    const toggleStatus = (id: string) => {
        setApps(prev => prev.map(app =>
            app.id === id ? { ...app, enabled: !app.enabled } : app
        ));
    };

    return {
        apps: filteredApps,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        toggleStatus,
        refetch: fetchData
    };
};
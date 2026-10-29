// src/features/dashboard/hooks/useDashboard.ts
import { useState, useEffect, useCallback } from 'react';
import { dashboardApi } from '../api/dashboardApi';

export const useDashboard = (selectedDate: Date) => {
    const [data, setData] = useState<any>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            const result = await dashboardApi.getDashboardData(selectedDate);
            setData(result);
        } catch (err: any) {
            setError(err.message || 'Lỗi tải dữ liệu.');
        } finally {
            setIsLoading(false);
        }
    }, [selectedDate]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    return {
        data,
        isLoading,
        error,
        refetch: fetchData,
    };
};
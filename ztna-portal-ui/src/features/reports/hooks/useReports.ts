// src/features/reports/hooks/useReports.ts
import { useState, useEffect, useCallback } from 'react';
import { reportApi } from '../api/reportApi';

export const useReports = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [data, setData] = useState<any>(null);

    // State cho Bộ lọc
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [selectedApp, setSelectedApp] = useState('ALL');

    const fetchReport = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            const res = await reportApi.getReportData(fromDate, toDate, selectedApp);
            setData(res);
        } catch (err: any) {
            setError(err.message || 'Lỗi tải báo cáo');
        } finally {
            setIsLoading(false);
        }
    }, [fromDate, toDate, selectedApp]);

    useEffect(() => {
        fetchReport();
    }, [fetchReport]);

    return {
        data,
        isLoading,
        error,
        refetch: fetchReport,
        filters: {
            fromDate, setFromDate,
            toDate, setToDate,
            selectedApp, setSelectedApp
        }
    };
};
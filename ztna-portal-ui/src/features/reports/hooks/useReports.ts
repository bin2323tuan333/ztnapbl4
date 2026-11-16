// src/features/reports/hooks/useReports.ts
import { useState, useEffect, useCallback } from 'react';
import { reportApi, type ReportData, type ResourceOption } from '../api/reportApi';

export const useReports = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [data, setData] = useState<ReportData | null>(null);
    const [resources, setResources] = useState<ResourceOption[]>([]);

    // State cho Bộ lọc
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [selectedApp, setSelectedApp] = useState('ALL');   // 'ALL' hoặc id ứng dụng

    // Danh sách ứng dụng cho ô lọc: chỉ tải 1 lần
    useEffect(() => {
        reportApi.getResources().then(setResources).catch(() => setResources([]));
    }, []);

    const fetchReport = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            setData(await reportApi.getReportData(fromDate, toDate, selectedApp));
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
        resources,
        filters: {
            fromDate, setFromDate,
            toDate, setToDate,
            selectedApp, setSelectedApp
        }
    };
};

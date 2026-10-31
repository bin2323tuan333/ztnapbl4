import { useState, useEffect, useCallback, useMemo } from 'react';
import { connectorApi, type ConnectorSummary } from '../api/connectorApi';

export const useConnectors = () => {
    const [connectors, setConnectors] = useState<ConnectorSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await connectorApi.getConnectors();
            setConnectors(data);
            setError('');
        } catch (err: any) {
            setError(err.message || 'Lỗi tải dữ liệu Connectors');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const filteredConnectors = useMemo(() => {
        if (!searchTerm) return connectors;
        const lower = searchTerm.toLowerCase();
        return connectors.filter(c => c.name.toLowerCase().includes(lower));
    }, [connectors, searchTerm]);

    const toggleStatus = async (id: string, currentStatus: boolean) => {
        // Optimistic UI Update
        setConnectors(prev => prev.map(c => c.id === id ? { ...c, enabled: !currentStatus } : c));
        try {
            await connectorApi.toggleStatus(id);
        } catch (err) {
            // Rollback nếu lỗi
            setConnectors(prev => prev.map(c => c.id === id ? { ...c, enabled: currentStatus } : c));
            alert('Không thể đổi trạng thái, vui lòng thử lại!');
        }
    };

    const deleteConnector = async (id: string) => {
        await connectorApi.deleteConnector(id);
        setConnectors(prev => prev.filter(c => c.id !== id));
    };

    return {
        connectors: filteredConnectors,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        toggleStatus,
        deleteConnector,
        refetch: fetchData
    };
};
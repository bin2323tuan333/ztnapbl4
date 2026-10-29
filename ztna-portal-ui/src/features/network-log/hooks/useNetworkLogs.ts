import { useState, useEffect, useCallback } from 'react';
import { logApi, type NetworkLog, type RouteTraceData } from '../api/logApi';

export const useNetworkLogs = () => {
    const [logs, setLogs] = useState<NetworkLog[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedLogId, setSelectedLogId] = useState<string | null>(null);
    const [routeData, setRouteData] = useState<RouteTraceData | null>(null);
    const [isRouteLoading, setIsRouteLoading] = useState(false);

    useEffect(() => {
        const fetchLogs = async () => {
            try {
                const data = await logApi.getLogs();
                setLogs(data);
            } catch (err) {
                console.error(err);
            } finally {
                setIsLoading(false);
            }
        };
        fetchLogs();
    }, []);

    const handleSelectLog = useCallback(async (logId: string) => {
        setSelectedLogId(logId);
        setIsRouteLoading(true);
        try {
            // Tìm dòng log tương ứng
            const selectedLog = logs.find(l => l.id === logId);
            if (selectedLog) {
                // Truyền cả object log xuống API
                const trace = await logApi.getRouteTrace(selectedLog);
                setRouteData(trace);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setIsRouteLoading(false);
        }
    }, [logs]);

    const handleCloseTrace = useCallback(() => {
        setRouteData(null);
        setSelectedLogId(null);
    }, []);

    return {
        logs,
        isLoading,
        selectedLogId,
        handleSelectLog,
        routeData,
        isRouteLoading,
        handleCloseTrace
    };
};
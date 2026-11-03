import { useState, useEffect, useCallback } from 'react';
import { accessLogApi } from '../api/accessLogApi';
import type {
    AccessLog,
    AccessLogSearchParams,
    Decision,
} from '../api/accessLogApi';

const DEFAULT_SIZE = 20;

export const useAccessLogs = () => {
    const [logs, setLogs] = useState<AccessLog[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [username, setUsername] = useState('');
    const [resourceName, setResourceName] = useState('');
    const [decision, setDecision] = useState<Decision | ''>('');
    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');

    const [page, setPage] = useState(0);
    const [size] = useState(DEFAULT_SIZE);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const toIso = (local: string, endOfDay = false): string | undefined => {
        if (!local) return undefined;
        const d = new Date(local);
        if (Number.isNaN(d.getTime())) return undefined;
        if (endOfDay && local.length <= 10) {
            d.setHours(23, 59, 59, 999);
        }
        return d.toISOString();
    };

    const fetchLogs = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const params: AccessLogSearchParams = {
                username: username || undefined,
                resourceName: resourceName || undefined,
                decision: decision || undefined,
                from: toIso(from),
                to: toIso(to, true),
                page,
                size,
                sort: 'createdAt,desc',
            };
            const res = await accessLogApi.search(params);
            setLogs(res.content ?? []);
            setTotalElements(res.totalElements ?? 0);
            setTotalPages(res.totalPages ?? 0);
        } catch (err: unknown) {
            console.error(err);
            const message =
                err instanceof Error ? err.message : 'Không thể tải nhật ký truy cập';
            setError(message);
            setLogs([]);
            setTotalElements(0);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    }, [username, resourceName, decision, from, to, page, size]);

    useEffect(() => {
        void fetchLogs();
    }, [fetchLogs]);

    const applyFilters = useCallback(
        (next: {
            username?: string;
            resourceName?: string;
            decision?: Decision | '';
            from?: string;
            to?: string;
        }) => {
            if (next.username !== undefined) setUsername(next.username);
            if (next.resourceName !== undefined) setResourceName(next.resourceName);
            if (next.decision !== undefined) setDecision(next.decision);
            if (next.from !== undefined) setFrom(next.from);
            if (next.to !== undefined) setTo(next.to);
            setPage(0);
        },
        []
    );

    const resetFilters = useCallback(() => {
        setUsername('');
        setResourceName('');
        setDecision('');
        setFrom('');
        setTo('');
        setPage(0);
    }, []);

    return {
        logs,
        isLoading,
        error,
        username,
        resourceName,
        decision,
        from,
        to,
        applyFilters,
        resetFilters,
        page,
        size,
        totalElements,
        totalPages,
        setPage,
        refetch: fetchLogs,
    };
};
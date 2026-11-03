import { useState, useEffect, useCallback } from 'react';
import { auditApi } from '../api/auditApi';
import type {
    AuditLog,
    AuditSearchParams,
    AuditAction,
    TargetType,
} from '../api/auditApi';

const DEFAULT_SIZE = 20;

export const useAuditLogs = () => {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [actorUsername, setActorUsername] = useState('');
    const [action, setAction] = useState<AuditAction | ''>('');
    const [targetType, setTargetType] = useState<TargetType | ''>('');
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
            const params: AuditSearchParams = {
                actorUsername: actorUsername || undefined,
                action: action || undefined,
                targetType: targetType || undefined,
                from: toIso(from),
                to: toIso(to, true),
                page,
                size,
                sort: 'createdAt,desc',
            };
            const res = await auditApi.search(params);
            setLogs(res.content ?? []);
            setTotalElements(res.totalElements ?? 0);
            setTotalPages(res.totalPages ?? 0);
        } catch (err: unknown) {
            console.error(err);
            const message =
                err instanceof Error ? err.message : 'Không thể tải nhật ký kiểm toán';
            setError(message);
            setLogs([]);
            setTotalElements(0);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    }, [actorUsername, action, targetType, from, to, page, size]);

    useEffect(() => {
        void fetchLogs();
    }, [fetchLogs]);

    const applyFilters = useCallback(
        (next: {
            actorUsername?: string;
            action?: AuditAction | '';
            targetType?: TargetType | '';
            from?: string;
            to?: string;
        }) => {
            if (next.actorUsername !== undefined) setActorUsername(next.actorUsername);
            if (next.action !== undefined) setAction(next.action);
            if (next.targetType !== undefined) setTargetType(next.targetType);
            if (next.from !== undefined) setFrom(next.from);
            if (next.to !== undefined) setTo(next.to);
            setPage(0);
        },
        []
    );

    const resetFilters = useCallback(() => {
        setActorUsername('');
        setAction('');
        setTargetType('');
        setFrom('');
        setTo('');
        setPage(0);
    }, []);

    return {
        logs,
        isLoading,
        error,
        actorUsername,
        action,
        targetType,
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
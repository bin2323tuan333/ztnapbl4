import { getRequest } from '@/shared/utils/apiClient';

export type Decision = 'ALLOW' | 'DENY';

export interface AccessLog {
    id: string;
    userId: string | null;
    userName: string | null;
    userIp: string | null;
    resourceId: string | null;
    resourceName: string | null;
    resourceHost: string | null;
    connectorId: string | null;
    connectorName: string | null;
    decision: Decision | string;
    reason: string | null;
    createdAt: string;
}

export interface AccessLogSearchParams {
    username?: string;
    resourceName?: string;
    decision?: Decision | '';
    from?: string;
    to?: string;
    page?: number;
    size?: number;
    sort?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

const BASE = '/api/v1/admin/access-logs';

export const DECISION_OPTIONS: { value: Decision; label: string }[] = [
    { value: 'ALLOW', label: 'Cho phép (ALLOW)' },
    { value: 'DENY', label: 'Từ chối (DENY)' },
];

export function decisionBadgeClass(decision: string): string {
    if (decision === 'ALLOW') return 'green';
    if (decision === 'DENY') return 'red';
    return 'gray';
}

function buildQuery(params: AccessLogSearchParams): string {
    const q = new URLSearchParams();
    if (params.username?.trim()) q.set('username', params.username.trim());
    if (params.resourceName?.trim()) q.set('resourceName', params.resourceName.trim());
    if (params.decision) q.set('decision', params.decision);
    if (params.from) q.set('from', params.from);
    if (params.to) q.set('to', params.to);
    q.set('page', String(params.page ?? 0));
    q.set('size', String(params.size ?? 20));
    q.set('sort', params.sort ?? 'createdAt,desc');
    return q.toString();
}

export const accessLogApi = {
    search: (params: AccessLogSearchParams = {}): Promise<PageResponse<AccessLog>> => {
        return getRequest(`${BASE}?${buildQuery(params)}`);
    },
};
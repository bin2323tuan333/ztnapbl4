import { useState } from 'react';
import {
    RefreshCw,
    Filter,
    X,
    Search,
    ChevronLeft,
    ChevronRight,
    Shield,
} from 'lucide-react';
import { useAccessLogs } from '../hooks/useAccessLogs';
import { DECISION_OPTIONS, decisionBadgeClass } from '../api/accessLogApi';
import type { Decision } from '../api/accessLogApi';
import '../styles/AccessLogs.css';

const formatDateTime = (iso: string | null | undefined) => {
    if (!iso) return '—';
    try {
        return new Date(iso).toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
        });
    } catch {
        return iso;
    }
};

export const AccessLogsPage = () => {
    const {
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
        refetch,
    } = useAccessLogs();

    const [draftUser, setDraftUser] = useState(username);
    const [draftResource, setDraftResource] = useState(resourceName);
    const [draftDecision, setDraftDecision] = useState<Decision | ''>(decision);
    const [draftFrom, setDraftFrom] = useState(from);
    const [draftTo, setDraftTo] = useState(to);
    const [showFilters, setShowFilters] = useState(true);

    const handleApply = () => {
        applyFilters({
            username: draftUser,
            resourceName: draftResource,
            decision: draftDecision,
            from: draftFrom,
            to: draftTo,
        });
    };

    const handleReset = () => {
        setDraftUser('');
        setDraftResource('');
        setDraftDecision('');
        setDraftFrom('');
        setDraftTo('');
        resetFilters();
    };

    const hasActiveFilter = !!(username || resourceName || decision || from || to);
    const fromIdx = totalElements === 0 ? 0 : page * size + 1;
    const toIdx = Math.min((page + 1) * size, totalElements);

    return (
        <div className="access-page">
            <div className="access-header">
                <div className="access-header-left">
                    <Shield size={22} className="access-header-icon" />
                    <div>
                        <h1 className="access-title">Log truy cập</h1>
                        <p className="access-subtitle">
                            Lịch sử truy cập tài nguyên qua ZTNA (ALLOW / DENY)
                        </p>
                    </div>
                </div>
                <div className="access-header-actions">
                    <button
                        type="button"
                        className="access-btn ghost"
                        onClick={() => setShowFilters((v) => !v)}
                    >
                        <Filter size={16} />
                        {showFilters ? 'Ẩn bộ lọc' : 'Hiện bộ lọc'}
                    </button>
                    <button
                        type="button"
                        className="access-btn ghost"
                        onClick={() => void refetch()}
                        disabled={isLoading}
                        title="Làm mới"
                    >
                        <RefreshCw size={16} className={isLoading ? 'spin' : ''} />
                        Làm mới
                    </button>
                </div>
            </div>

            {showFilters && (
                <div className="access-filter-card">
                    <div className="access-filter-grid">
                        <label className="access-field">
                            <span>Người dùng</span>
                            <div className="access-input-with-icon">
                                <Search size={15} />
                                <input
                                    type="text"
                                    placeholder="Username..."
                                    value={draftUser}
                                    onChange={(e) => setDraftUser(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && handleApply()}
                                />
                            </div>
                        </label>

                        <label className="access-field">
                            <span>Tài nguyên</span>
                            <input
                                type="text"
                                placeholder="Tên resource..."
                                value={draftResource}
                                onChange={(e) => setDraftResource(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleApply()}
                            />
                        </label>

                        <label className="access-field">
                            <span>Quyết định</span>
                            <select
                                value={draftDecision}
                                onChange={(e) =>
                                    setDraftDecision(e.target.value as Decision | '')
                                }
                            >
                                <option value="">Tất cả</option>
                                {DECISION_OPTIONS.map((o) => (
                                    <option key={o.value} value={o.value}>
                                        {o.label}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="access-field">
                            <span>Từ thời điểm</span>
                            <input
                                type="datetime-local"
                                value={draftFrom}
                                onChange={(e) => setDraftFrom(e.target.value)}
                            />
                        </label>

                        <label className="access-field">
                            <span>Đến thời điểm</span>
                            <input
                                type="datetime-local"
                                value={draftTo}
                                onChange={(e) => setDraftTo(e.target.value)}
                            />
                        </label>
                    </div>

                    <div className="access-filter-actions">
                        {hasActiveFilter && (
                            <button type="button" className="access-btn ghost" onClick={handleReset}>
                                <X size={15} />
                                Xóa bộ lọc
                            </button>
                        )}
                        <button type="button" className="access-btn primary" onClick={handleApply}>
                            <Filter size={15} />
                            Áp dụng
                        </button>
                    </div>
                </div>
            )}

            {hasActiveFilter && (
                <div className="access-chips">
                    {username && (
                        <span className="access-chip">
                            User: <strong>{username}</strong>
                        </span>
                    )}
                    {resourceName && (
                        <span className="access-chip">
                            Resource: <strong>{resourceName}</strong>
                        </span>
                    )}
                    {decision && (
                        <span className="access-chip">
                            Decision: <strong>{decision}</strong>
                        </span>
                    )}
                    {from && (
                        <span className="access-chip">
                            Từ: <strong>{formatDateTime(new Date(from).toISOString())}</strong>
                        </span>
                    )}
                    {to && (
                        <span className="access-chip">
                            Đến: <strong>{formatDateTime(new Date(to).toISOString())}</strong>
                        </span>
                    )}
                </div>
            )}

            {error && <div className="access-error">{error}</div>}

            <div className="access-table-card">
                <div className="access-table-scroll">
                    <table className="access-table">
                        <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>User</th>
                                <th>Resource</th>
                                <th>Connector</th>
                                <th>Host</th>
                                <th>IP</th>
                                <th>Quyết định</th>
                                <th>Lý do</th>
                            </tr>
                        </thead>
                        <tbody>
                            {isLoading ? (
                                <tr>
                                    <td colSpan={8} className="access-empty">
                                        Đang tải dữ liệu...
                                    </td>
                                </tr>
                            ) : logs.length === 0 ? (
                                <tr>
                                    <td colSpan={8} className="access-empty">
                                        Không có bản ghi nào phù hợp với bộ lọc
                                    </td>
                                </tr>
                            ) : (
                                logs.map((log) => (
                                    <tr key={log.id}>
                                        <td className="access-mono">
                                            {formatDateTime(log.createdAt)}
                                        </td>
                                        <td>
                                            <div className="access-actor">
                                                <span className="access-actor-name">
                                                    {log.userName || '—'}
                                                </span>
                                                {log.userId && (
                                                    <span className="access-actor-id">
                                                        {log.userId.slice(0, 8)}…
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="access-actor">
                                                <span className="access-actor-name">
                                                    {log.resourceName || '—'}
                                                </span>
                                                {log.resourceId && (
                                                    <span className="access-actor-id">
                                                        {log.resourceId.slice(0, 8)}…
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            {log.connectorName || log.connectorId ? (
                                                <div className="access-actor">
                                                    <span className="access-actor-name">
                                                        {log.connectorName || '—'}
                                                    </span>
                                                    {log.connectorId && (
                                                        <span className="access-actor-id">
                                                            {log.connectorId.slice(0, 8)}…
                                                        </span>
                                                    )}
                                                </div>
                                            ) : (
                                                '—'
                                            )}
                                        </td>
                                        <td className="access-path" title={log.resourceHost ?? undefined}>
                                            {log.resourceHost || '—'}
                                        </td>
                                        <td className="access-mono">{log.userIp || '—'}</td>
                                        <td>
                                            <span
                                                className={`access-badge ${decisionBadgeClass(
                                                    String(log.decision)
                                                )}`}
                                            >
                                                {log.decision}
                                            </span>
                                        </td>
                                        <td className="access-reason" title={log.reason ?? undefined}>
                                            {log.reason || '—'}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="access-pagination">
                    <span className="access-page-info">
                        {totalElements === 0
                            ? '0 bản ghi'
                            : `${fromIdx} – ${toIdx} / ${totalElements} bản ghi`}
                    </span>
                    <div className="access-page-btns">
                        <button
                            type="button"
                            className="access-page-btn"
                            disabled={page <= 0 || isLoading}
                            onClick={() => setPage(page - 1)}
                        >
                            <ChevronLeft size={16} />
                        </button>
                        <span className="access-page-num">
                            {totalPages === 0 ? 0 : page + 1} / {totalPages}
                        </span>
                        <button
                            type="button"
                            className="access-page-btn"
                            disabled={page >= totalPages - 1 || isLoading || totalPages === 0}
                            onClick={() => setPage(page + 1)}
                        >
                            <ChevronRight size={16} />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};
import { useState } from 'react';
import {
    RefreshCw,
    Filter,
    X,
    Search,
    ChevronLeft,
    ChevronRight,
    ScrollText,
} from 'lucide-react';
import { useAuditLogs } from '../hooks/useAuditLogs';
import { ACTION_OPTIONS, TARGET_TYPE_OPTIONS, actionBadgeClass } from '../api/auditApi';
import type { AuditAction, TargetType } from '../api/auditApi';
import '../styles/AuditLogs.css';

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

export const AuditLogsPage = () => {
    const {
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
        refetch,
    } = useAuditLogs();

    const [draftUser, setDraftUser] = useState(actorUsername);
    const [draftAction, setDraftAction] = useState<AuditAction | ''>(action);
    const [draftTarget, setDraftTarget] = useState<TargetType | ''>(targetType);
    const [draftFrom, setDraftFrom] = useState(from);
    const [draftTo, setDraftTo] = useState(to);
    const [showFilters, setShowFilters] = useState(true);

    const handleApply = () => {
        applyFilters({
            actorUsername: draftUser,
            action: draftAction,
            targetType: draftTarget,
            from: draftFrom,
            to: draftTo,
        });
    };

    const handleReset = () => {
        setDraftUser('');
        setDraftAction('');
        setDraftTarget('');
        setDraftFrom('');
        setDraftTo('');
        resetFilters();
    };

    const hasActiveFilter = !!(actorUsername || action || targetType || from || to);
    const fromIdx = totalElements === 0 ? 0 : page * size + 1;
    const toIdx = Math.min((page + 1) * size, totalElements);

    return (
        <div className="audit-page">
            <div className="audit-header">
                <div className="audit-header-left">
                    <ScrollText size={22} className="audit-header-icon" />
                    <div>
                        <h1 className="audit-title">Log kiểm toán</h1>
                        <p className="audit-subtitle">
                            Theo dõi mọi thao tác quản trị trên hệ thống ZTNA
                        </p>
                    </div>
                </div>
                <div className="audit-header-actions">
                    <button
                        type="button"
                        className="audit-btn ghost"
                        onClick={() => setShowFilters((v) => !v)}
                    >
                        <Filter size={16} />
                        {showFilters ? 'Ẩn bộ lọc' : 'Hiện bộ lọc'}
                    </button>
                    <button
                        type="button"
                        className="audit-btn ghost"
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
                <div className="audit-filter-card">
                    <div className="audit-filter-grid">
                        <label className="audit-field">
                            <span>Người thực hiện</span>
                            <div className="audit-input-with-icon">
                                <Search size={15} />
                                <input
                                    type="text"
                                    placeholder="Tên đăng nhập..."
                                    value={draftUser}
                                    onChange={(e) => setDraftUser(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && handleApply()}
                                />
                            </div>
                        </label>

                        <label className="audit-field">
                            <span>Hành động</span>
                            <select
                                value={draftAction}
                                onChange={(e) =>
                                    setDraftAction(e.target.value as AuditAction | '')
                                }
                            >
                                <option value="">Tất cả hành động</option>
                                {ACTION_OPTIONS.map((o) => (
                                    <option key={o.value} value={o.value}>
                                        {o.label}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="audit-field">
                            <span>Đối tượng</span>
                            <select
                                value={draftTarget}
                                onChange={(e) =>
                                    setDraftTarget(e.target.value as TargetType | '')
                                }
                            >
                                <option value="">Tất cả đối tượng</option>
                                {TARGET_TYPE_OPTIONS.map((o) => (
                                    <option key={o.value} value={o.value}>
                                        {o.label}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="audit-field">
                            <span>Từ thời điểm</span>
                            <input
                                type="datetime-local"
                                value={draftFrom}
                                onChange={(e) => setDraftFrom(e.target.value)}
                            />
                        </label>

                        <label className="audit-field">
                            <span>Đến thời điểm</span>
                            <input
                                type="datetime-local"
                                value={draftTo}
                                onChange={(e) => setDraftTo(e.target.value)}
                            />
                        </label>
                    </div>

                    <div className="audit-filter-actions">
                        {hasActiveFilter && (
                            <button type="button" className="audit-btn ghost" onClick={handleReset}>
                                <X size={15} />
                                Xóa bộ lọc
                            </button>
                        )}
                        <button type="button" className="audit-btn primary" onClick={handleApply}>
                            <Filter size={15} />
                            Áp dụng
                        </button>
                    </div>
                </div>
            )}

            {hasActiveFilter && (
                <div className="audit-chips">
                    {actorUsername && (
                        <span className="audit-chip">
                            User: <strong>{actorUsername}</strong>
                        </span>
                    )}
                    {action && (
                        <span className="audit-chip">
                            Action: <strong>{action}</strong>
                        </span>
                    )}
                    {targetType && (
                        <span className="audit-chip">
                            Target: <strong>{targetType}</strong>
                        </span>
                    )}
                    {from && (
                        <span className="audit-chip">
                            Từ: <strong>{formatDateTime(new Date(from).toISOString())}</strong>
                        </span>
                    )}
                    {to && (
                        <span className="audit-chip">
                            Đến: <strong>{formatDateTime(new Date(to).toISOString())}</strong>
                        </span>
                    )}
                </div>
            )}

            {error && <div className="audit-error">{error}</div>}

            <div className="audit-table-card">
                <div className="audit-table-scroll">
                    <table className="audit-table">
                        <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>Người thực hiện</th>
                                <th>Hành động</th>
                                <th>Đối tượng</th>
                                <th>Chi tiết</th>
                                <th>IP</th>
                            </tr>
                        </thead>
                        <tbody>
                            {isLoading ? (
                                <tr>
                                    <td colSpan={6} className="audit-empty">
                                        Đang tải dữ liệu...
                                    </td>
                                </tr>
                            ) : logs.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="audit-empty">
                                        Không có bản ghi nào phù hợp với bộ lọc
                                    </td>
                                </tr>
                            ) : (
                                logs.map((log) => (
                                    <tr key={log.id}>
                                        <td className="audit-mono">
                                            {formatDateTime(log.createdAt)}
                                        </td>
                                        <td>
                                            <div className="audit-actor">
                                                <span className="audit-actor-name">
                                                    {log.actorUsername || '—'}
                                                </span>
                                                {log.actorId && (
                                                    <span className="audit-actor-id">
                                                        {log.actorId.slice(0, 8)}…
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            <span
                                                className={`audit-badge ${actionBadgeClass(
                                                    log.action
                                                )}`}
                                            >
                                                {log.action}
                                            </span>
                                        </td>
                                        <td>
                                            {log.targetType ? (
                                                <span className="audit-target">
                                                    <span className="audit-badge gray">
                                                        {log.targetType}
                                                    </span>
                                                    {log.targetId && (
                                                        <span className="audit-target-id">
                                                            {log.targetId.length > 12
                                                                ? log.targetId.slice(0, 12) + '…'
                                                                : log.targetId}
                                                        </span>
                                                    )}
                                                </span>
                                            ) : (
                                                '—'
                                            )}
                                        </td>
                                        <td className="audit-details" title={log.details}>
                                            {log.details || '—'}
                                        </td>
                                        <td className="audit-mono">{log.ipAddress || '—'}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="audit-pagination">
                    <span className="audit-page-info">
                        {totalElements === 0
                            ? '0 bản ghi'
                            : `${fromIdx} – ${toIdx} / ${totalElements} bản ghi`}
                    </span>
                    <div className="audit-page-btns">
                        <button
                            type="button"
                            className="audit-page-btn"
                            disabled={page <= 0 || isLoading}
                            onClick={() => setPage(page - 1)}
                        >
                            <ChevronLeft size={16} />
                        </button>
                        <span className="audit-page-num">
                            {totalPages === 0 ? 0 : page + 1} / {totalPages}
                        </span>
                        <button
                            type="button"
                            className="audit-page-btn"
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
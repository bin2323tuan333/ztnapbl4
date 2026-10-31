import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Modal from '@/components/common/Modal';
import { SearchBar } from '@/components/common/SearchBar';
import type { PermissionItem, PermissionType, PermissionAction } from '../api/authorizationApi';
import { usePermissions, type PermissionScope } from '../hooks/usePermissions';
import '@/shared/styles/AdminPage.css';

const SCOPE_TEXT: Record<PermissionScope, { title: string; subtitle: string }> = {
    all: { title: 'Quản lý quyền', subtitle: 'Tất cả quyền truy cập ứng dụng: cấp theo vai trò và cấp riêng cho người dùng' },
    role: { title: 'Cấp quyền theo Vai trò', subtitle: 'Mọi thành viên của vai trò đều được vào ứng dụng' },
    user: { title: 'Quyền riêng người dùng', subtitle: 'Quyền cấp trực tiếp cho một người dùng' },
    resource: { title: 'Cấp quyền theo Ứng dụng', subtitle: 'Chọn một ứng dụng để xem ai đang được truy cập' },
    expiring: { title: 'Quyền sắp hết hạn', subtitle: 'Quyền còn hiệu lực nhưng hết hạn trong 7 ngày tới' },
};

const EMPTY_GRANT = {
    type: 'ROLE' as PermissionType,
    targetId: '',
    resourceId: '',
    action: 'READ' as PermissionAction,
    reason: '',
    expiresAt: '',
};

const formatDate = (v: string | null) => (v ? new Date(v).toLocaleString('vi-VN') : 'Không hết hạn');

interface Props {
    scope: PermissionScope;
}

export const PermissionsPage = ({ scope }: Props) => {
    const { perms, roles, users, resources, isLoading, error, searchTerm, setSearchTerm,
        resourceFilter, setResourceFilter, grantToRole, grantToUser, revoke } = usePermissions(scope);

    const [showGrant, setShowGrant] = useState(false);
    const [grant, setGrant] = useState(EMPTY_GRANT);
    const [grantError, setGrantError] = useState('');
    const [saving, setSaving] = useState(false);

    // Mở form: chọn sẵn loại theo trang đang đứng, và ứng dụng đang lọc (nếu có)
    const openGrant = () => {
        setGrant({
            ...EMPTY_GRANT,
            type: scope === 'user' ? 'USER' : 'ROLE',
            resourceId: scope === 'resource' ? resourceFilter : '',
        });
        setGrantError('');
        setShowGrant(true);
    };

    const handleGrant = async (e: React.FormEvent) => {
        e.preventDefault();
        setGrantError('');
        setSaving(true);
        // Ngày hết hạn tính đến cuối ngày đã chọn
        const expiresAt = grant.expiresAt ? new Date(`${grant.expiresAt}T23:59:59`).toISOString() : null;
        try {
            if (grant.type === 'ROLE') {
                await grantToRole({ roleId: grant.targetId, resourceId: grant.resourceId, action: grant.action, reason: grant.reason, expiresAt });
            } else {
                await grantToUser({ userId: grant.targetId, resourceId: grant.resourceId, reason: grant.reason, expiresAt });
            }
            setShowGrant(false);
        } catch (err: any) {
            setGrantError(err.message || 'Cấp quyền thất bại');
        } finally {
            setSaving(false);
        }
    };

    const handleRevoke = async (p: PermissionItem) => {
        const target = p.type === 'ROLE' ? `vai trò ${p.roleName}` : `người dùng ${p.username}`;
        if (!window.confirm(`Thu hồi quyền vào "${p.resourceName}" của ${target}?`)) return;
        try { await revoke(p); } catch (err: any) { alert(err.message); }
    };

    const { title, subtitle } = SCOPE_TEXT[scope];

    return (
        <div className="ap-container">
            <div className="ap-header">
                <h1 className="ap-title">{title}</h1>
                <p className="ap-subtitle">{subtitle}</p>
            </div>

            <div className="ap-toolbar">
                <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
                    <SearchBar value={searchTerm} onChange={setSearchTerm} placeholder="Tìm theo vai trò, người dùng, ứng dụng, lý do..." />
                    {scope === 'resource' && (
                        <select className="ap-input" style={{ width: 260 }} value={resourceFilter}
                                onChange={(e) => setResourceFilter(e.target.value)}>
                            <option value="">— Tất cả ứng dụng —</option>
                            {resources.map(r => <option key={r.id} value={r.id}>{r.name} ({r.type})</option>)}
                        </select>
                    )}
                </div>
                <button className="ap-btn" onClick={openGrant}><Plus size={16} /> Cấp quyền</button>
            </div>

            {error && <div className="ap-error">{error}</div>}

            <div className="ap-table-wrapper">
                <table className="ap-table">
                    <thead>
                        <tr>
                            <th>Đối tượng</th>
                            <th>Ứng dụng</th>
                            <th>Hành động</th>
                            <th>Cấp bởi</th>
                            <th>Hết hạn</th>
                            <th>Lý do</th>
                            <th>Trạng thái</th>
                            <th style={{ textAlign: 'right' }}>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={8} className="ap-empty">Đang tải...</td></tr>
                        ) : perms.length === 0 ? (
                            <tr><td colSpan={8} className="ap-empty">Chưa có quyền nào</td></tr>
                        ) : perms.map(p => (
                            <tr key={`${p.type}-${p.id}`}>
                                <td>
                                    {p.type === 'ROLE'
                                        ? <><span className="ap-badge blue">Vai trò</span> <strong>{p.roleName}</strong></>
                                        : <><span className="ap-badge yellow">Người dùng</span> <strong>{p.username}</strong></>}
                                </td>
                                <td>{p.resourceName}</td>
                                <td className="ap-muted">{p.action ?? 'READ'}</td>
                                <td className="ap-muted">{p.grantedBy}</td>
                                <td className="ap-muted">{formatDate(p.expiresAt)}</td>
                                <td className="ap-muted">{p.reason}</td>
                                <td>
                                    <span className={`ap-badge ${p.active ? 'green' : 'gray'}`}>
                                        {p.active ? 'Hiệu lực' : (p.enabled ? 'Hết hạn' : 'Đã tắt')}
                                    </span>
                                </td>
                                <td>
                                    <div className="ap-actions">
                                        <button className="ap-icon-btn danger" title="Thu hồi" onClick={() => handleRevoke(p)}>
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Cấp quyền */}
            <Modal isOpen={showGrant} onClose={() => setShowGrant(false)} title="Cấp quyền truy cập ứng dụng">
                <form className="ap-form" onSubmit={handleGrant}>
                    {grantError && <div className="ap-error">{grantError}</div>}

                    <select className="ap-input" value={grant.type}
                            onChange={(e) => setGrant({ ...grant, type: e.target.value as PermissionType, targetId: '' })}>
                        <option value="ROLE">Cấp cho vai trò</option>
                        <option value="USER">Cấp riêng cho 1 người dùng</option>
                    </select>

                    <select className="ap-input" value={grant.targetId}
                            onChange={(e) => setGrant({ ...grant, targetId: e.target.value })}>
                        <option value="">{grant.type === 'ROLE' ? '— Chọn vai trò —' : '— Chọn người dùng —'}</option>
                        {grant.type === 'ROLE'
                            ? roles.map(r => <option key={r.id} value={r.id}>{r.name} — {r.displayName}</option>)
                            : users.map(u => <option key={u.id} value={u.id}>{u.username} — {u.fullName}</option>)}
                    </select>

                    <select className="ap-input" value={grant.resourceId}
                            onChange={(e) => setGrant({ ...grant, resourceId: e.target.value })}>
                        <option value="">— Chọn ứng dụng —</option>
                        {resources.map(r => (
                            <option key={r.id} value={r.id}>{r.name} ({r.type}){r.enabled ? '' : ' — đang tắt'}</option>
                        ))}
                    </select>

                    {grant.type === 'ROLE' && (
                        <select className="ap-input" value={grant.action}
                                onChange={(e) => setGrant({ ...grant, action: e.target.value as PermissionAction })}>
                            <option value="READ">READ — Chỉ đọc</option>
                            <option value="WRITE">WRITE — Ghi</option>
                            <option value="DELETE">DELETE — Xóa</option>
                            <option value="EXECUTE">EXECUTE — Thực thi</option>
                        </select>
                    )}

                    <input className="ap-input" placeholder="Lý do (bắt buộc)" value={grant.reason}
                           onChange={(e) => setGrant({ ...grant, reason: e.target.value })} />
                    <label className="ap-label">
                        Hết hạn (bỏ trống = không hết hạn)
                        <input className="ap-input" type="date" value={grant.expiresAt}
                               onChange={(e) => setGrant({ ...grant, expiresAt: e.target.value })} />
                    </label>

                    <button type="submit" className="ap-btn" disabled={saving || !grant.targetId || !grant.resourceId}>
                        {saving ? 'Đang lưu...' : 'Cấp quyền'}
                    </button>
                </form>
            </Modal>
        </div>
    );
};

import { useState, useEffect } from 'react';
import { X, Edit, User, AppWindow, Info } from 'lucide-react';
import { ruleApi, type RuleDetail, type UserInfo, type ResourceInfo } from '../api/ruleApi';
import { EditRuleAssignmentModal } from './EditRuleAssignmentModal';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    ruleId: string | null;
    onRefresh: () => void;
}

export const RuleDetailModal = ({ isOpen, onClose, ruleId, onRefresh }: Props) => {
    const [detail, setDetail] = useState<RuleDetail | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const [editType, setEditType] = useState<'user' | 'resource' | null>(null);
    const [allUsers, setAllUsers] = useState<UserInfo[]>([]);
    const [allResources, setAllResources] = useState<ResourceInfo[]>([]);

    const loadData = async () => {
        if (!isOpen || !ruleId) return;
        setIsLoading(true);
        try {
            const data = await ruleApi.getRuleDetail(ruleId);
            setDetail(data);
            const [users, res] = await Promise.all([ruleApi.getAllUsers(), ruleApi.getAllResources()]);
            setAllUsers(users);
            setAllResources(res);
        } catch (err) {
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { loadData(); }, [isOpen, ruleId]);

    if (!isOpen) return null;

    const handleSaveAssignments = async (selectedIds: string[]) => {
        if (!ruleId || !editType) return;
        if (editType === 'user') await ruleApi.updateRuleUsers(ruleId, selectedIds);
        else await ruleApi.updateRuleResources(ruleId, selectedIds);
        await loadData();
        onRefresh();
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="detail-modal-content" style={{ maxWidth: '1000px', width: '95%' }} onClick={e => e.stopPropagation()}>

                <div className="detail-modal-header">
                    <div className="detail-modal-title">
                        <div className="modal-title-icon" style={{ background: '#eff6ff', color: '#3b82f6' }}>
                            <Info size={24} />
                        </div>
                        <div>
                            <h2>{isLoading ? 'Đang tải...' : detail?.name}</h2>
                            <span className="status-badge" data-active={detail?.enabled}>
                                {detail?.enabled ? 'Active' : 'Inactive'}
                            </span>
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>

                {detail && (
                    <div style={{ padding: '24px' }}>
                        {/* KHỐI 1: THÔNG TIN CƠ BẢN */}
                        <div style={{ background: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <span style={{ fontSize: '13px', color: '#64748b' }}>Mô tả: </span>
                                <span style={{ fontSize: '14px', fontWeight: 500 }}>{detail.description || 'Không có'}</span>
                            </div>
                            <div style={{ fontSize: '13px', color: '#64748b' }}>
                                Ngày tạo: {new Date(detail.createdAt).toLocaleString('vi-VN')}
                            </div>
                        </div>

                        {/* KHỐI 2 & 3: 2 CỘT USER VÀ RESOURCE */}
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>

                            {/* Cột User */}
                            <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                                <div style={{ padding: '12px 16px', background: '#f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e2e8f0' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600, color: '#334155' }}>
                                        <User size={18} /> Users ({detail.users?.length || 0})
                                    </div>
                                    <button className="icon-btn" onClick={() => setEditType('user')} style={{ color: '#3b82f6' }} title="Sửa Users">
                                        <Edit size={18} />
                                    </button>
                                </div>
                                <div style={{ padding: '16px', height: '300px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px', background: '#fff' }}>
                                    {detail.users?.map(u => (
                                        <div key={u.id} style={{ padding: '10px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '6px', fontSize: '14px' }}>
                                            <span style={{ fontWeight: 500 }}>{u.username}</span> <span style={{ color: '#94a3b8', fontSize: '12px' }}>({u.email})</span>
                                        </div>
                                    ))}
                                    {detail.users?.length === 0 && <div className="empty-text">Chưa có user nào.</div>}
                                </div>
                            </div>

                            {/* Cột Resource */}
                            <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                                <div style={{ padding: '12px 16px', background: '#f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e2e8f0' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600, color: '#334155' }}>
                                        <AppWindow size={18} /> Applications ({detail.resources?.length || 0})
                                    </div>
                                    <button className="icon-btn" onClick={() => setEditType('resource')} style={{ color: '#3b82f6' }} title="Sửa Apps">
                                        <Edit size={18} />
                                    </button>
                                </div>
                                <div style={{ padding: '16px', height: '300px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px', background: '#fff' }}>
                                    {detail.resources?.map(r => (
                                        <div key={r.id} style={{ padding: '10px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '6px', fontSize: '14px' }}>
                                            <span style={{ fontWeight: 500 }}>{r.name}</span> <span style={{ color: '#94a3b8', fontSize: '12px' }}>[{r.type}]</span>
                                        </div>
                                    ))}
                                    {detail.resources?.length === 0 && <div className="empty-text">Chưa có app nào.</div>}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* MODAL CON ĐỂ CHỈNH SỬA (DẠNG 2 CỘT TRANSFER) */}
            {editType && detail && (
                <EditRuleAssignmentModal
                    isOpen={!!editType}
                    onClose={() => setEditType(null)}
                    onSave={handleSaveAssignments}
                    title={editType === 'user' ? 'Chọn Người dùng' : 'Chọn Ứng dụng'}
                    allItems={editType === 'user' ? allUsers : allResources}
                    initialSelectedIds={editType === 'user' ? detail.users.map(u => u.id) : detail.resources.map(r => r.id)}
                    itemType={editType}
                />
            )}
        </div>
    );
};
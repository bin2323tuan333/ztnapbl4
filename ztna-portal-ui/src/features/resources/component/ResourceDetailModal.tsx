import { useState, useEffect } from 'react';
import { X, Globe, Database, Terminal, Server, Clock, Info, Edit, Trash2, AlertCircle } from 'lucide-react';
import { resourceApi, type ResourceDetail } from '../api/resourceApi';
import '../styles/ResourceDetailModal.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    resourceId: string | null;
    onEdit: (id: string) => void;
    onDelete: (id: string) => void;
}

export const ResourceDetailModal = ({ isOpen, onClose, resourceId, onEdit, onDelete }: Props) => {
    const [detail, setDetail] = useState<ResourceDetail | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        let isMounted = true; // Cờ chống Race Condition

        if (isOpen && resourceId) {
            setIsLoading(true);
            setError(''); // Xóa lỗi cũ
            setDetail(null); // Xóa data cũ

            resourceApi.getResourceDetail(resourceId)
                .then(data => {
                    if (isMounted) setDetail(data);
                })
                .catch(err => {
                    if (isMounted) setError(err.message || 'Lỗi tải chi tiết. Resource có thể đã bị xóa.');
                })
                .finally(() => {
                    if (isMounted) setIsLoading(false);
                });
        } else {
            // Reset sạch sẽ khi đóng
            setDetail(null);
            setError('');
            setIsLoading(false);
        }

        return () => {
            isMounted = false; // Hủy bỏ update state nếu component unmount hoặc id thay đổi
        };
    }, [isOpen, resourceId]);

    if (!isOpen) return null;

    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString('vi-VN');
    };

    const getIcon = (type?: string) => {
        switch (type) {
            case 'WEB': return <Globe size={24} />;
            case 'MYSQL': return <Database size={24} />;
            case 'SSH': return <Terminal size={24} />;
            default: return <Server size={24} />;
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="detail-modal-content" onClick={e => e.stopPropagation()}>

                {/* HEADER LUÔN HIỂN THỊ (Để luôn có nút X) */}
                <div className="detail-modal-header" style={{ borderBottom: (!isLoading && !error && detail) ? '1px solid #e2e8f0' : 'none' }}>
                    <div className="detail-modal-title">
                        {(!isLoading && !error && detail) ? (
                            <>
                                <div className="icon-wrapper">{getIcon(detail.type)}</div>
                                <div>
                                    <h2>{detail.name}</h2>
                                    <span className="status-badge" data-active={detail.enabled}>
                                        {detail.enabled ? 'Active' : 'Inactive'}
                                    </span>
                                </div>
                            </>
                        ) : (
                            <h2>{isLoading ? 'Đang tải...' : 'Chi tiết tài nguyên'}</h2>
                        )}
                    </div>

                    <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        {/* Chỉ hiện nút Sửa/Xóa khi có data */}
                        {(!isLoading && !error && detail) && (
                            <>
                                <button className="icon-btn" title="Edit" onClick={() => onEdit(detail.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#3b82f6' }}>
                                    <Edit size={18} />
                                </button>
                                <button className="icon-btn danger" title="Delete" onClick={() => {
                                    if (window.confirm('Chắc chắn xóa ứng dụng này?')) onDelete(detail.id);
                                }} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}>
                                    <Trash2 size={18} />
                                </button>
                            </>
                        )}
                        {/* Nút X luôn ở đây */}
                        <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                    </div>
                </div>

                {/* BODY */}
                {isLoading && (
                    <div className="modal-loading" style={{ padding: '40px', textAlign: 'center' }}>Đang tải dữ liệu...</div>
                )}

                {!isLoading && error && (
                    <div className="modal-error" style={{ padding: '40px', textAlign: 'center', color: '#b91c1c' }}>
                        <AlertCircle size={40} style={{ margin: '0 auto 12px', color: '#ef4444' }} />
                        <h3 style={{ margin: '0 0 8px 0', color: '#0f172a' }}>Không thể tải dữ liệu</h3>
                        <p style={{ fontSize: '14px' }}>{error}</p>
                        <button className="btn-outline" onClick={onClose} style={{ marginTop: '16px' }}>Đóng</button>
                    </div>
                )}

                {!isLoading && !error && detail && (
                    <div className="detail-modal-body">
                        <p className="section-title"><Info size={16} /> General Information</p>
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="info-label">Type</span>
                                <span className="info-value">{detail.type}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Host</span>
                                <span className="info-value">{detail.host}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Port</span>
                                <span className="info-value">{detail.port}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Connector Zone</span>
                                <span className="info-value">{detail.connectorZone || 'Default'}</span>
                            </div>
                        </div>

                        <p className="section-title" style={{ marginTop: '24px' }}>Description</p>
                        <div className="description-box">
                            {detail.description ? <p>{detail.description}</p> : <p className="text-muted italic">Không có mô tả.</p>}
                        </div>

                        <div className="metadata-footer">
                            <div className="meta-item"><Clock size={14} /> <span>Created: {formatDate(detail.createdAt)}</span></div>
                            <div className="meta-item"><Clock size={14} /> <span>Updated: {formatDate(detail.updatedAt)}</span></div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
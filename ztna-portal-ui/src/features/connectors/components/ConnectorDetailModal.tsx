import { useState, useEffect } from 'react';
import { X, Clock, Info, Server, Link } from 'lucide-react';
import { connectorApi, type ConnectorDetail } from '../api/connectorApi';
import { ConnectorIcon } from './ConnectorIcon';
import '../styles/ConnectorDetailModal.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    connectorId: string | null;
}

export const ConnectorDetailModal = ({ isOpen, onClose, connectorId }: Props) => {
    const [detail, setDetail] = useState<ConnectorDetail | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        let isMounted = true;
        if (isOpen && connectorId) {
            setIsLoading(true);
            setError('');
            connectorApi.getConnectorById(connectorId)
                .then(data => { if (isMounted) setDetail(data); })
                .catch(err => { if (isMounted) setError(err.message || 'Lỗi tải chi tiết'); })
                .finally(() => { if (isMounted) setIsLoading(false); });
        } else {
            setDetail(null);
        }
        return () => { isMounted = false; };
    }, [isOpen, connectorId]);

    if (!isOpen) return null;

    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString('vi-VN');
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="detail-modal-content" onClick={e => e.stopPropagation()}>
                <div className="detail-modal-header">
                    <div className="detail-modal-title">
                        <ConnectorIcon size={24} />
                        <div>
                            <h2>{isLoading ? 'Đang tải...' : (detail?.name || 'Chi tiết Connector')}</h2>
                            {!isLoading && detail && (
                                <span className="status-badge" data-active={detail.enabled}>
                                    {detail.enabled ? 'Active' : 'Inactive'}
                                </span>
                            )}
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>

                {isLoading && <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải dữ liệu...</div>}
                {error && <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>{error}</div>}

                {!isLoading && !error && detail && (
                    <div className="detail-modal-body">
                        <p className="section-title"><Info size={16} /> Cấu hình kết nối</p>

                        <div className="info-item" style={{ marginBottom: '16px' }}>
                            <span className="info-label"><Link size={14} /> Connection String</span>
                            <div className="description-box font-mono" style={{ marginTop: '8px' }}>
                                {detail.connectionString}
                            </div>
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
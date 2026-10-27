import { useState } from 'react';
import Modal from '../common/Modal';
import type { Resource } from '../../types/model/Resource';
// Import thêm các icon
import { ExternalLink, Trash2, Monitor, Database, Globe, Terminal } from 'lucide-react';

interface Props {
    resource: Resource | null;
    isOpen: boolean;
    onClose: () => void;
    onDelete?: (id: string) => void;
}

const AGENT_URL = 'http://127.0.0.1:9999/open';

const AppDetailModal = ({ resource, isOpen, onClose, onDelete }: Props) => {
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

    if (!resource) return null;

    const getIcon = () => {
        switch (resource.type.toLowerCase()) {
            case 'ssh': return <Terminal size={40} />;
            case 'mysql': return <Database size={40} />;
            case 'web': return <Globe size={40} />;
            default: return <Monitor size={40} />;
        }
    };

    const handleAccess = async () => {
        setLoading(true);
        setStatus(null);

        try {
            const jwt = localStorage.getItem('accessToken') || '';

            const url = `${AGENT_URL}?appId=${encodeURIComponent(resource.id)}&type=${encodeURIComponent(resource.type.toLowerCase())}`;

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${jwt}`
                }
            });

            const data = await response.json();

            if (response.ok && data.status === 'success') {
                setStatus({
                    type: 'success',
                    message: `Đã mở "${resource.name}" tại port ${data.port}`,
                });
            } else {
                throw new Error(data.error || data.message || 'Lỗi không xác định từ Agent');
            }
        } catch (err) {
            const msg =
                err instanceof Error
                    ? err.message
                    : 'Không kết nối được Agent. Kiểm tra Agent đã chạy ở port 9999 chưa?';
            setStatus({ type: 'error', message: msg });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Chi tiết ứng dụng">
            <div style={{ textAlign: 'center', padding: '10px 0' }}>
                <div style={{
                    width: '80px', height: '80px', borderRadius: '20px',
                    backgroundColor: '#e0f2fe', color: '#0284c7',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    margin: '0 auto 20px auto'
                }}>
                    {/* Gọi hàm getIcon ở đây */}
                    {getIcon()}
                </div>

                <h2 style={{ margin: '0 0 10px 0', color: '#1f2937' }}>{resource.name}</h2>
                <p style={{ color: '#6b7280', marginBottom: '20px' }}>
                    {resource.description || 'Không có mô tả cho ứng dụng này.'}
                </p>

                {status && (
                    <div style={{
                        marginBottom: '16px',
                        padding: '12px',
                        borderRadius: '8px',
                        backgroundColor: status.type === 'success' ? '#dcfce7' : '#fee2e2',
                        color: status.type === 'success' ? '#166534' : '#991b1b',
                        fontSize: '14px',
                        wordBreak: 'break-word',
                    }}>
                        {status.type === 'success' ? '✅ ' : '❌ '}
                        {status.message}
                    </div>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <button
                        onClick={handleAccess}
                        disabled={!resource.enabled || loading}
                        style={{
                            padding: '12px',
                            background: resource.enabled && !loading ? '#0284c7' : '#9ca3af',
                            color: 'white', border: 'none', borderRadius: '8px',
                            fontSize: '15px', fontWeight: 500,
                            cursor: resource.enabled && !loading ? 'pointer' : 'not-allowed',
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px',
                        }}
                    >
                        <ExternalLink size={18} />
                        {loading ? 'Đang kết nối...' : 'Truy cập ứng dụng'}
                    </button>

                    {onDelete && (
                        <button
                            onClick={() => onDelete(resource.id)}
                            style={{
                                padding: '12px', background: '#fee2e2', color: '#dc2626',
                                border: 'none', borderRadius: '8px', fontSize: '15px',
                                cursor: 'pointer', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px',
                                marginTop: '8px',
                            }}
                        >
                            <Trash2 size={18} /> Xóa ứng dụng
                        </button>
                    )}

                    <button
                        onClick={onClose}
                        style={{
                            padding: '12px', background: 'transparent', color: '#6b7280',
                            border: 'none', fontSize: '15px', cursor: 'pointer', marginTop: '-4px',
                        }}
                    >
                        Đóng
                    </button>
                </div>
            </div>
        </Modal>
    );
};

export default AppDetailModal;
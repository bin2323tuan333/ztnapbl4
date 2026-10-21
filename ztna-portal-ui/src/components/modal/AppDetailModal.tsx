import Modal from '../common/Modal';
import type { Resource } from '../../types/model/Resource';
import { ExternalLink, Trash2, Monitor, Laptop } from 'lucide-react'; // Thêm icon Laptop

interface Props {
    resource: Resource | null;
    isOpen: boolean;
    onClose: () => void;
    onDelete?: (id: string) => void;
}

const AppDetailModal = ({ resource, isOpen, onClose, onDelete }: Props) => {
    if (!resource) return null;

    // Xử lý khi bấm nút truy cập
    const handleAccessWeb = () => {
        const resourceId = resource.id;

        const ztnaProxyUrl = `https://localhost:9443/resource/${resourceId}`;

        window.open(ztnaProxyUrl, '_blank');
    };

    const handleAccessAgent = () => {
        alert("Đang kết nối qua ZTNA Agent...");
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Chi tiết ứng dụng">
            <div style={{ textAlign: 'center', padding: '10px 0' }}>
                {/* Icon App */}
                <div style={{
                    width: '80px', height: '80px', borderRadius: '20px',
                    backgroundColor: '#e0f2fe', color: '#0284c7',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    margin: '0 auto 20px auto'
                }}>
                    <Monitor size={40} />
                </div>

                <h2 style={{ margin: '0 0 10px 0', color: '#1f2937' }}>{resource.name}</h2>
                <p style={{ color: '#6b7280', marginBottom: '30px' }}>
                    {resource.description || 'Không có mô tả cho ứng dụng này.'}
                </p>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <button
                        onClick={handleAccessWeb}
                        disabled={!resource.enabled}
                        style={{
                            padding: '12px',
                            background: resource.enabled ? '#0284c7' : '#9ca3af',
                            color: 'white', border: 'none', borderRadius: '8px',
                            fontSize: '15px', fontWeight: 500,
                            cursor: resource.enabled ? 'pointer' : 'not-allowed',
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px'
                        }}
                    >
                        <ExternalLink size={18} />
                        Truy cập bằng Web
                    </button>

                    <button
                        onClick={handleAccessAgent}
                        disabled={!resource.enabled}
                        style={{
                            padding: '12px',
                            background: resource.enabled ? '#f3f4f6' : '#f9fafb',
                            color: resource.enabled ? '#1f2937' : '#9ca3af',
                            border: resource.enabled ? '1px solid #d1d5db' : '1px solid #e5e7eb',
                            borderRadius: '8px',
                            fontSize: '15px', fontWeight: 500,
                            cursor: resource.enabled ? 'pointer' : 'not-allowed',
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px'
                        }}
                    >
                        <Laptop size={18} />
                        Truy cập bằng Agent
                    </button>

                    {onDelete && (
                        <button
                            onClick={() => onDelete(resource.id)}
                            style={{
                                padding: '12px', background: '#fee2e2', color: '#dc2626',
                                border: 'none', borderRadius: '8px', fontSize: '15px',
                                cursor: 'pointer', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px',
                                marginTop: '8px'
                            }}
                        >
                            <Trash2 size={18} /> Xóa ứng dụng
                        </button>
                    )}

                    <button
                        onClick={onClose}
                        style={{
                            padding: '12px', background: 'transparent', color: '#6b7280',
                            border: 'none', fontSize: '15px', cursor: 'pointer', marginTop: '-4px'
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
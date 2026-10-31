import { useState } from 'react';
import { Search, Plus } from 'lucide-react';
import { useConnectors } from '../hooks/useConnectors';
import '../styles/Connectors.css';
import { ConnectorIcon } from '../components/ConnectorIcon';
import { ConnectorRow } from '../components/ConnectorRow';
import { ConnectorFormModal } from '../components/ConnectorFormModal';
import { ConnectorDetailModal } from '../components/ConnectorDetailModal';

export const ConnectorsPage = () => {
    const { connectors, isLoading, error, searchTerm, setSearchTerm, toggleStatus, deleteConnector, refetch } = useConnectors();

    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [viewingId, setViewingId] = useState<string | null>(null); // State cho Modal Xem chi tiết

    const handleCreate = () => {
        setEditingId(null);
        setIsFormOpen(true);
    };

    const handleEdit = (id: string) => {
        setViewingId(null); // Đóng modal chi tiết nếu đang mở
        setEditingId(id);
        setIsFormOpen(true);
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('Chắc chắn xóa Connector này? Các Resource sử dụng nó có thể bị ảnh hưởng.')) {
            await deleteConnector(id);
            setViewingId(null);
        }
    };

    return (
        <div className="resources-container">
            <div className="resources-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <ConnectorIcon size={24} />
                    <h1 className="resources-title">Connectors</h1>
                </div>
            </div>

            <div className="resources-toolbar">
                <div className="search-box">
                    <Search size={16} className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search connectors..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="toolbar-actions">
                    <button className="btn-black" onClick={handleCreate}>
                        <Plus size={16} /> New Connector
                    </button>
                </div>
            </div>

            {error && <div className="error-msg">{error}</div>}

            <div className="resources-table-wrapper">
                <table className="resources-table">
                    <thead>
                        <tr>
                            <th>Trạng thái</th>
                            <th>Connector Name</th>
                            <th>Ngày tạo</th>
                            <th style={{ textAlign: 'center' }}>Hành động</th>
                            <th style={{ textAlign: 'center' }}>Bật/Tắt</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={5} className="text-center py-8">Đang tải...</td></tr>
                        ) : connectors.length === 0 ? (
                            <tr><td colSpan={5} className="text-center py-8">Chưa có Connector nào.</td></tr>
                        ) : (
                            connectors.map(c => (
                                <ConnectorRow
                                    key={c.id}
                                    connector={c}
                                    onToggle={() => toggleStatus(c.id, c.enabled)}
                                    onEdit={handleEdit}
                                    onDelete={handleDelete}
                                    onView={(id) => setViewingId(id)}
                                />
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Modal Form (Thêm/Sửa) */}
            <ConnectorFormModal
                isOpen={isFormOpen}
                onClose={() => setIsFormOpen(false)}
                onSuccess={refetch}
                connectorId={editingId}
            />

            {/* Modal Detail (Xem chi tiết) */}
            <ConnectorDetailModal
                isOpen={!!viewingId}
                onClose={() => setViewingId(null)}
                connectorId={viewingId}
            />
        </div>
    );
};
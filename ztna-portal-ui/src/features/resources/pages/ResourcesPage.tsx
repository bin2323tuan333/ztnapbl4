import { useState } from 'react';
import { Search, Plus } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi';
import '../styles/Resources.css';
import { useResources } from '../hooks/useResource';
import { ResourceRow } from '../component/ResourceRow';
import { ApplicationTypeModal } from '../component/ApplicationTypeModal';
import { ResourceDetailModal } from '../component/ResourceDetailModal';
import { ResourceFormModal } from '../component/ResourceFormModal';

export const ResourcesPage = () => {
    const { apps, isLoading, error, searchTerm, setSearchTerm, toggleStatus, deleteApp, refetch } = useResources();

    const [isTypeModalOpen, setIsTypeModalOpen] = useState(false);
    const [isFormModalOpen, setIsFormModalOpen] = useState(false);

    // Chỉ cần lưu ID thay vì cả Object
    const [editingId, setEditingId] = useState<string | null>(null);
    const [selectedResourceId, setSelectedResourceId] = useState<string | null>(null);
    const [newResourceType, setNewResourceType] = useState<ResourceType>('WEB');

    const handleSelectType = (type: ResourceType) => {
        setNewResourceType(type);
        setIsTypeModalOpen(false);
        setEditingId(null); // Thêm mới thì không có ID
        setIsFormModalOpen(true);
    };

    // Chỉ cần truyền ID cho Form, Form sẽ tự gọi API lấy data mới nhất
    const handleEditClick = (id: string) => {
        setSelectedResourceId(null); // Đóng Modal chi tiết
        setEditingId(id);            // Gán ID để Form biết là đang Edit
        setIsFormModalOpen(true);    // Mở Form
    };

    const handleDelete = async (id: string) => {
        await deleteApp(id);
        setSelectedResourceId(null);
    };

    const handleCloseForm = () => {
        setIsFormModalOpen(false);
        setEditingId(null); // Reset ID khi đóng form
    };

    return (
        <div className="resources-container">
            <div className="resources-header">
                <h1 className="resources-title">Self-Hosted Applications</h1>
            </div>

            <div className="resources-toolbar">
                <div className="search-box">
                    <Search size={16} className="search-icon" />
                    <input type="text" placeholder="Search..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                </div>
                <div className="toolbar-actions">
                    <button className="btn-black" onClick={() => setIsTypeModalOpen(true)}>
                        <Plus size={16} /> New Application
                    </button>
                </div>
            </div>

            {error && <div className="error-msg">{error}</div>}

            <div className="resources-table-wrapper">
                <table className="resources-table">
                    <thead>
                        <tr>
                            <th>Hoạt động</th>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Host</th>
                            <th>Port</th>
                            <th>Connector Zone</th>
                            <th style={{ textAlign: 'center' }}>Bật/Tắt</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={7} className="text-center py-8">Đang tải...</td></tr>
                        ) : apps.length === 0 ? (
                            <tr><td colSpan={7} className="text-center py-8">Không tìm thấy ứng dụng nào.</td></tr>
                        ) : (
                            apps.map(app => (
                                <ResourceRow
                                    key={app.id}
                                    app={app}
                                    onToggle={() => toggleStatus(app.id, app.enabled)}
                                    onRowClick={() => setSelectedResourceId(app.id)}
                                />
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <ApplicationTypeModal
                isOpen={isTypeModalOpen}
                onClose={() => setIsTypeModalOpen(false)}
                onSelectType={handleSelectType}
            />

            {/* ĐÃ SỬA LẠI PROPS CHO ĐÚNG VỚI STATE CỦA BẠN */}
            <ResourceFormModal
                isOpen={isFormModalOpen}
                onClose={handleCloseForm}
                onSuccess={refetch}
                resourceId={editingId}
                defaultType={newResourceType}
            />

            <ResourceDetailModal
                isOpen={!!selectedResourceId}
                onClose={() => setSelectedResourceId(null)}
                resourceId={selectedResourceId}
                onEdit={handleEditClick}
                onDelete={handleDelete}
            />
        </div>
    );
};
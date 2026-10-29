import { useState } from 'react';
import { Search, Plus } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi'; // Import type
import '../styles/Resources.css';
import { useResources } from '../hooks/useResource';
import { ResourceRow } from '../component/ResourceRow';
import { ApplicationTypeModal } from '../component/ApplicationTypeModal';

export const ResourcesPage = () => {
    const { apps, isLoading, error, searchTerm, setSearchTerm, toggleStatus } = useResources();

    // State quản lý đóng/mở Modal
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Hàm xử lý khi chọn loại ứng dụng
    const handleSelectType = (type: ResourceType) => {
        setIsModalOpen(false);
        console.log('Đã chọn loại ứng dụng:', type);
        // TODO: Chuyển hướng sang trang tạo mới với form tương ứng
        // Ví dụ: navigate(`/app/resources/new?type=${type}`);
    };

    return (
        <div className="resources-container">
            <div className="resources-header">
                <h1 className="resources-title">Self-Hosted Applications</h1>
            </div>

            {/* Toolbar */}
            <div className="resources-toolbar">
                <div className="search-box">
                    <Search size={16} className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="toolbar-actions">
                    {/* Gắn sự kiện mở Modal vào nút này */}
                    <button className="btn-black" onClick={() => setIsModalOpen(true)}>
                        <Plus size={16} /> New Application
                    </button>
                </div>
            </div>

            {error && <div className="error-msg">{error}</div>}

            {/* Table */}
            <div className="resources-table-wrapper">
                <table className="resources-table">
                    <thead>
                        <tr>
                            <th style={{ width: '80px' }}>Enabled</th>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Domain / IP Address</th>
                            <th>Connector Zone</th>
                            {/* Đã xóa Identity Provider và Tags, thêm Actions */}
                            <th style={{ width: '120px', textAlign: 'right' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={6} className="text-center py-8">Đang tải...</td></tr>
                        ) : apps.length === 0 ? (
                            <tr><td colSpan={6} className="text-center py-8">Không tìm thấy ứng dụng nào.</td></tr>
                        ) : (
                            apps.map(app => (
                                <ResourceRow
                                    key={app.id}
                                    app={app}
                                    onToggle={() => toggleStatus(app.id)}
                                />
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Render Modal */}
            <ApplicationTypeModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSelectType={handleSelectType}
            />
        </div>
    );
};
import React from 'react';
import type { Resource } from '../../types/model/Resource';
import { Monitor, Database, Globe, Terminal } from 'lucide-react'; // Thêm icon
import './ResourceCard.css';

interface Props {
    resource: Resource;
    onClick: (resource: Resource) => void;
}

const ResourceCard = ({ resource, onClick }: Props) => {
    // (Tùy chọn) Đổi icon dựa theo type
    const getIcon = () => {
        switch (resource.type.toLowerCase()) {
            case 'ssh': return <Terminal size={24} />;
            case 'mysql': return <Database size={24} />;
            case 'web': return <Globe size={24} />;
            default: return <Monitor size={24} />;
        }
    };

    return (
        <div
            className={`resource-card ${!resource.enabled ? 'disabled' : ''}`}
            onClick={() => onClick(resource)}
        >
            <div className="card-header">
                <div className="header-left">
                    <div className="card-icon">
                        {getIcon()}
                    </div>
                    <h3 className="card-title" title={resource.name}>
                        {resource.name}
                    </h3>
                </div>

                {/* Badge Type ở góc trên bên phải */}
                <span className={`type-badge type-${resource.type.toLowerCase()}`}>
                    {resource.type}
                </span>
            </div>

            <div className="card-body">
                <p className="card-description">
                    {resource.description || 'Không có mô tả'}
                </p>
            </div>

            <div className="card-footer">
                <span className={`status-badge ${resource.enabled ? 'active' : 'inactive'}`}>
                    {resource.enabled ? 'Đang hoạt động' : 'Đã khóa'}
                </span>
            </div>
        </div>
    );
};

export default ResourceCard;
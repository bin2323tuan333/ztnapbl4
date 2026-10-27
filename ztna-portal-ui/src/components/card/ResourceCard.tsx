import React from 'react';
import type { Resource } from '../../types/model/Resource';
import { Monitor, Lock } from 'lucide-react';
import './ResourceCard.css';

interface Props {
    resource: Resource;
    onClick: (resource: Resource) => void;
}

const ResourceCard = ({ resource, onClick }: Props) => {
    return (
        <div
            className={`resource-card ${!resource.enabled ? 'disabled' : ''}`}
            onClick={() => onClick(resource)}
        >
            <div className="card-header">
                <div className="card-icon">
                    <Monitor size={24} />
                </div>
                <h3 className="card-title" title={resource.name}>
                    {resource.name}
                </h3>
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
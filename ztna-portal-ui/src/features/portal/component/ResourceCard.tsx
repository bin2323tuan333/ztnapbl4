import { Monitor, Database, Globe, Terminal } from 'lucide-react';
import type { Resource } from '@/shared/types/model/Resource';
import '../styles/ResourceCard.css';

interface Props {
    resource: Resource;
    onClick: (resource: Resource) => void;
}

export const ResourceCard = ({ resource, onClick }: Props) => {

    const getIcon = () => {
        switch (resource.type.toLowerCase()) {
            case 'ssh': return <Terminal size={22} />;
            case 'mysql': return <Database size={22} />;
            case 'web': return <Globe size={22} />;
            default: return <Monitor size={22} />;
        }
    };

    return (
        <div
            className={`resource-card ${!resource.enabled ? 'disabled' : ''}`}
            onClick={() => resource.enabled && onClick(resource)}
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

                <span className={`type-badge type-${resource.type.toLowerCase()}`}>
                    {resource.type}
                </span>
            </div>

            <div className="card-body">
                <p className="card-description">
                    {resource.description || 'Không có mô tả cho ứng dụng này.'}
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
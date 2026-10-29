import { Globe, Network, Monitor, Terminal, Search, X } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi';
import '../styles/ApplicationTypeModal.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSelectType: (type: ResourceType) => void;
}

// Danh sách các loại ứng dụng (Bạn có thể thêm/bớt tùy ý)
const APP_TYPES = [
    { type: 'SELF_HOSTED_WEB', label: 'Self-Hosted Web Application', icon: Globe, color: '#a855f7', bg: '#f3e8ff' },
    { type: 'NETWORK_RANGE', label: 'Network Range', icon: Network, color: '#64748b', bg: '#f1f5f9' },
    { type: 'RDP', label: 'RDP - Remote Desktop', icon: Monitor, color: '#ef4444', bg: '#fee2e2' },
    { type: 'SSH', label: 'SSH Server', icon: Terminal, color: '#1e293b', bg: '#f3f4f6' },
];

export const ApplicationTypeModal = ({ isOpen, onClose, onSelectType }: Props) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                {/* Header */}
                <div className="modal-header">
                    <h2 className="modal-title">New Application</h2>
                    <button className="modal-close-btn" onClick={onClose}>
                        <X size={20} />
                    </button>
                </div>

                {/* Search Bar */}
                <div className="modal-search">
                    <Search size={16} className="search-icon" />
                    <input type="text" placeholder="Search..." />
                </div>

                {/* Content (Chỉ có 1 tab duy nhất, không hiển thị thanh chuyển tab) */}
                <div className="modal-body">
                    <p className="section-title">Choose an application type</p>

                    <div className="app-types-grid">
                        {APP_TYPES.map((app) => (
                            <div
                                key={app.type}
                                className="app-type-card"
                                onClick={() => onSelectType(app.type as ResourceType)}
                            >
                                <div className="app-type-icon" style={{ backgroundColor: app.bg, color: app.color }}>
                                    <app.icon size={24} />
                                </div>
                                <div className="app-type-info">
                                    <span className="app-type-label">{app.label}</span>
                                    <span className="app-type-badge">Agentless Supported</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Footer */}
                <div className="modal-footer">
                    <button className="btn-outline" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};
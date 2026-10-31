import { Globe, Database, Terminal, X } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi';
import '../styles/ApplicationTypeModal.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSelectType: (type: ResourceType) => void;
}

const APP_TYPES = [
    { type: 'WEB', label: 'Web Application', icon: Globe, color: '#a855f7', bg: '#f3e8ff' },
    { type: 'MYSQL', label: 'MySQL Database', icon: Database, color: '#64748b', bg: '#f1f5f9' },
    { type: 'SSH', label: 'SSH Server', icon: Terminal, color: '#1e293b', bg: '#f3f4f6' },
];

export const ApplicationTypeModal = ({ isOpen, onClose, onSelectType }: Props) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">New Application</h2>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>
                <div className="modal-body">
                    <p className="section-title">Choose an application type</p>
                    <div className="app-types-grid">
                        {APP_TYPES.map((app) => (
                            <div key={app.type} className="app-type-card" onClick={() => onSelectType(app.type as ResourceType)}>
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
                <div className="modal-footer">
                    <button className="btn-outline" onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};
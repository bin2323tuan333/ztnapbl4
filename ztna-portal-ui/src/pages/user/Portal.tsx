import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    ShieldCheck, Search, ChevronDown, LogOut,
    Globe, Monitor, X, ExternalLink, Download,
    Server, Terminal, AppWindow, Database, Cloud, Lock,
    Wifi,
} from 'lucide-react';
import './Portal.css';

// ============ MOCK APP DATA ============
type App = {
    id: string;
    name: string;
    description: string;
    type: 'Web' | 'SSH' | 'RDP' | 'Database';
    connector_zone: string;
    backend_url: string;
    icon: React.ReactNode;
    color: string;
};

const ALL_APPS: App[] = [
    {
        id: 'app_web_01',
        name: 'Axis Fabric Composer',
        description: 'Web app quản trị hạ tầng mạng',
        type: 'Web',
        connector_zone: 'Homelab',
        backend_url: 'https://10.10.30.217',
        icon: <AppWindow size={26} />,
        color: '#9333ea',
    },
    {
        id: 'app_ssh_02',
        name: 'SSH WAN Simulator',
        description: 'SSH vào máy chủ test',
        type: 'SSH',
        connector_zone: 'Homelab',
        backend_url: 'ssh://10.10.30.230',
        icon: <Terminal size={26} />,
        color: '#0891b2',
    },
    {
        id: 'app_rdp_03',
        name: 'Remote Desktop',
        description: 'RDP vào Windows Server',
        type: 'RDP',
        connector_zone: 'Homelab',
        backend_url: 'rdp://10.10.30.101',
        icon: <Monitor size={26} />,
        color: '#ea580c',
    },
    {
        id: 'app_db_04',
        name: 'PostgreSQL Prod',
        description: 'Database production',
        type: 'Database',
        connector_zone: 'AWS-SG',
        backend_url: 'postgres://db.prod.local:5432',
        icon: <Database size={26} />,
        color: '#16a34a',
    },
    {
        id: 'app_web_05',
        name: 'Grafana Dashboard',
        description: 'Giám sát hệ thống',
        type: 'Web',
        connector_zone: 'AWS-SG',
        backend_url: 'https://grafana.local',
        icon: <Cloud size={26} />,
        color: '#f59e0b',
    },
    {
        id: 'app_web_06',
        name: 'Vault Secrets',
        description: 'Quản lý secrets nội bộ',
        type: 'Web',
        connector_zone: 'Homelab',
        backend_url: 'https://vault.local',
        icon: <Lock size={26} />,
        color: '#dc2626',
    },
];

// ============ COMPONENT ============
function Portal() {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [menuOpen, setMenuOpen] = useState(false);
    const [selectedApp, setSelectedApp] = useState<App | null>(null);

    const username = localStorage.getItem('username') || 'User';
    const allowedApps: string[] = JSON.parse(localStorage.getItem('allowedApps') || '[]');

    const visibleApps = ALL_APPS
        .filter(app => allowedApps.includes(app.id))
        .filter(app =>
            app.name.toLowerCase().includes(search.toLowerCase()) ||
            app.description.toLowerCase().includes(search.toLowerCase())
        );

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    const handleLaunchWeb = () => {
        alert(`🌐 Đang mở ${selectedApp?.name} qua trình duyệt...\n\nURL: ${selectedApp?.backend_url}`);
        setSelectedApp(null);
    };

    const handleLaunchAgent = () => {
        alert(`💻 Đang kết nối tới ${selectedApp?.name} qua Agent Local...\n\nAgent sẽ tự động mở kết nối ZTNA tunnel.`);
        setSelectedApp(null);
    };

    return (
        <div className="portal-page">
            {/* ============ HEADER ============ */}
            <header className="portal-header">
                <div className="portal-brand">
                    <div className="portal-brand-icon">
                        <ShieldCheck size={20} color="white" />
                    </div>
                    <span className="portal-brand-text">ZTNA Portal</span>
                </div>

                <div className="portal-user-wrapper">
                    <button
                        className="portal-user"
                        onClick={() => setMenuOpen(p => !p)}
                    >
                        <div className="portal-avatar">
                            {username[0]?.toUpperCase()}
                        </div>
                        <span className="portal-username">{username}</span>
                        <ChevronDown size={14} className="portal-chevron" />
                    </button>

                    {menuOpen && (
                        <div className="portal-dropdown">
                            <button className="portal-dropdown-item" onClick={handleLogout}>
                                <LogOut size={16} />
                                Đăng xuất
                            </button>
                        </div>
                    )}
                </div>
            </header>

            {/* ============ CONTENT ============ */}
            <main className="portal-main">
                <div className="portal-container">
                    {/* Search */}
                    <div className="portal-search-wrap">
                        <div className="portal-search">
                            <Search size={18} className="portal-search-icon" />
                            <input
                                type="text"
                                placeholder="Tìm kiếm ứng dụng..."
                                value={search}
                                onChange={e => setSearch(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* App grid */}
                    {visibleApps.length === 0 ? (
                        <div className="portal-empty">
                            <ShieldCheck size={56} className="portal-empty-icon" />
                            <h2>
                                {allowedApps.length === 0
                                    ? 'Chưa có ứng dụng nào được cấp'
                                    : 'Không tìm thấy ứng dụng'}
                            </h2>
                            <p>
                                {allowedApps.length === 0
                                    ? 'Vui lòng liên hệ quản trị viên để được cấp quyền truy cập.'
                                    : 'Thử tìm với từ khóa khác.'}
                            </p>
                        </div>
                    ) : (
                        <div className="portal-app-grid">
                            {visibleApps.map(app => (
                                <button
                                    key={app.id}
                                    className="portal-app-card"
                                    onClick={() => setSelectedApp(app)}
                                >
                                    <div
                                        className="portal-app-icon"
                                        style={{
                                            backgroundColor: `${app.color}15`,
                                            color: app.color,
                                        }}
                                    >
                                        {app.icon}
                                    </div>

                                    <div className="portal-app-info">
                                        <h3 className="portal-app-name">{app.name}</h3>
                                        <p className="portal-app-desc">{app.description}</p>
                                        <div className="portal-app-meta">
                                            <span className="portal-app-type">{app.type}</span>
                                            <span className="portal-app-zone">
                                                <Wifi size={11} /> {app.connector_zone}
                                            </span>
                                        </div>
                                    </div>
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </main>

            {/* ============ MODAL: Chọn cách truy cập ============ */}
            {selectedApp && (
                <div
                    className="portal-modal-overlay"
                    onClick={() => setSelectedApp(null)}
                >
                    <div
                        className="portal-modal"
                        onClick={e => e.stopPropagation()}
                    >
                        <button
                            className="portal-modal-close"
                            onClick={() => setSelectedApp(null)}
                            aria-label="Đóng"
                        >
                            <X size={18} />
                        </button>

                        <div className="portal-modal-header">
                            <div
                                className="portal-modal-icon"
                                style={{
                                    backgroundColor: `${selectedApp.color}15`,
                                    color: selectedApp.color,
                                }}
                            >
                                {selectedApp.icon}
                            </div>
                            <div>
                                <h2 className="portal-modal-title">{selectedApp.name}</h2>
                                <p className="portal-modal-desc">{selectedApp.description}</p>
                            </div>
                        </div>

                        <div className="portal-modal-question">
                            Bạn muốn truy cập ứng dụng này bằng cách nào?
                        </div>

                        <div className="portal-modal-options">
                            <button
                                className="portal-option"
                                onClick={handleLaunchWeb}
                            >
                                <div className="portal-option-icon portal-option-icon-web">
                                    <Globe size={28} />
                                </div>
                                <div className="portal-option-content">
                                    <h4>Sử dụng Web</h4>
                                    <p>Truy cập trực tiếp qua trình duyệt</p>
                                </div>
                                <ExternalLink size={16} className="portal-option-arrow" />
                            </button>

                            <button
                                className="portal-option"
                                onClick={handleLaunchAgent}
                            >
                                <div className="portal-option-icon portal-option-icon-agent">
                                    <Server size={28} />
                                </div>
                                <div className="portal-option-content">
                                    <h4>Sử dụng Agent Local</h4>
                                    <p>Kết nối qua ứng dụng ZTNA trên máy</p>
                                </div>
                                <Download size={16} className="portal-option-arrow" />
                            </button>
                        </div>

                        <div className="portal-modal-footer">
                            <div className="portal-modal-info">
                                <span className="portal-modal-info-label">Connector Zone:</span>
                                <span className="portal-modal-info-value">{selectedApp.connector_zone}</span>
                            </div>
                            <div className="portal-modal-info">
                                <span className="portal-modal-info-label">Backend:</span>
                                <code className="portal-modal-info-code">{selectedApp.backend_url}</code>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Portal;
import { PanelLeft } from 'lucide-react';
import { ADMIN_MENU } from './menuConfig';
import SidebarItem from './SidebarItem';
import './Sidebar.css';

interface SidebarProps {
    collapsed: boolean;
    onToggle: () => void;
}

const Sidebar = ({ collapsed, onToggle }: SidebarProps) => {
    return (
        <aside className={`sb-sidebar ${collapsed ? 'sb-collapsed' : ''}`}>
            <button
                className="sb-toggle"
                onClick={onToggle}
                title={collapsed ? 'Mở rộng' : 'Thu gọn'}
                aria-label={collapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'}
            >
                <PanelLeft size={16} />
            </button>

            <div className="sb-body">
                <nav className="sb-nav">
                    {ADMIN_MENU.map((item, index) => (
                        <SidebarItem
                            key={index}
                            item={item}
                            collapsed={collapsed}
                            onRequestExpand={collapsed ? onToggle : undefined}
                        />
                    ))}
                </nav>
            </div>
        </aside>
    );
};

export default Sidebar;
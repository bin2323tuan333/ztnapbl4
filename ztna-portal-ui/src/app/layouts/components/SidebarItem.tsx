import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ChevronDown } from 'lucide-react';
import type { MenuItem } from './menuConfig';

interface SidebarItemProps {
    item: MenuItem;
    collapsed: boolean;
    /** Gọi khi user click item có children lúc đang collapsed → nhờ cha mở sidebar */
    onRequestExpand?: () => void;
}

const isPathMatch = (current: string, target?: string): boolean => {
    if (!target) return false;
    const norm = (s: string) => s.replace(/\/+$/, '') || '/';
    return norm(current) === norm(target);
};

const SidebarItem = ({ item, collapsed, onRequestExpand }: SidebarItemProps) => {
    const location = useLocation();
    const navigate = useNavigate();
    const current = location.pathname + location.search;

    const hasChildren = !!item.children?.length;

    const isChildActive = hasChildren
        ? item.children!.some((c) => isPathMatch(current, c.path))
        : false;
    const isSelfActive = isPathMatch(current, item.path);

    const [isOpen, setIsOpen] = useState(isChildActive);

    useEffect(() => {
        if (isChildActive) setIsOpen(true);
    }, [isChildActive]);

    const handleClick = () => {
        if (hasChildren) {
            if (collapsed) {
                // Đang collapsed → nhờ cha mở sidebar + mở dropdown luôn
                onRequestExpand?.();
                setIsOpen(true);
            } else {
                // Đang mở → toggle bình thường
                setIsOpen((prev) => !prev);
            }
        } else if (item.path) {
            navigate(item.path);
        }
    };

    return (
        <div className="sb-item-wrapper">
            <div
                className={`sb-item ${isSelfActive || isChildActive ? 'sb-item-active' : ''}`}
                onClick={handleClick}
                title={collapsed ? item.label : undefined}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        handleClick();
                    }
                }}
            >
                {item.icon && <span className="sb-item-icon">{item.icon}</span>}

                {!collapsed && (
                    <>
                        <span className="sb-item-label">{item.label}</span>
                        {hasChildren && (
                            <ChevronDown
                                size={16}
                                className={`sb-item-chevron ${isOpen ? 'sb-item-chevron-open' : ''}`}
                            />
                        )}
                    </>
                )}
            </div>

            {hasChildren && !collapsed && (
                <div className={`sb-submenu ${isOpen ? 'sb-submenu-open' : ''}`}>
                    <div className="sb-submenu-inner">
                        {item.children!.map((child) => (
                            <div
                                key={child.label}
                                className={`sb-submenu-item ${isPathMatch(current, child.path) ? 'sb-submenu-item-active' : ''}`}
                                onClick={() => child.path && navigate(child.path)}
                                role="button"
                                tabIndex={0}
                                onKeyDown={(e) => {
                                    if ((e.key === 'Enter' || e.key === ' ') && child.path) {
                                        e.preventDefault();
                                        navigate(child.path);
                                    }
                                }}
                            >
                                <span className="sb-submenu-dot" />
                                <span>{child.label}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default SidebarItem;
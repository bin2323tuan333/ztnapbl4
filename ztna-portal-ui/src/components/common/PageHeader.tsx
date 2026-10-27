import type { ReactNode } from 'react';
import './PageHeader.css';

interface PageHeaderProps {
    title: string;
    subtitle?: string;
    actions?: ReactNode;
}

const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, actions }) => {
    return (
        <div className="ph-container">
            <div className="ph-text">
                <h1 className="ph-title">{title}</h1>
                {subtitle && <p className="ph-subtitle">{subtitle}</p>}
            </div>
            {actions && <div className="ph-actions">{actions}</div>}
        </div>
    );
};

export default PageHeader;
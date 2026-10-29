import { ExternalLink, Edit, Trash2, MoreHorizontal } from 'lucide-react';
import type { Application } from '../api/resourceApi';
import { ToggleSwitch } from './ToggleSwitch';
import { ResourceIcon } from './ResourceIcon';

interface Props {
    app: Application;
    onToggle: () => void;
}

export const ResourceRow = ({ app, onToggle }: Props) => {
    return (
        <tr className="resource-row">
            <td><ToggleSwitch checked={app.enabled} onChange={onToggle} /></td>
            <td>
                <div className="app-name-cell">
                    <ResourceIcon type={app.type} />
                    <span className="app-name">{app.name}</span>
                    <ExternalLink size={14} className="external-icon" />
                </div>
            </td>
            <td className="col-type">{app.type.replace(/_/g, ' ')}</td>
            <td className="col-domain">{app.domain}</td>
            <td className="text-muted">{app.connectorZone}</td>

            {/* Cụm nút Action nằm ở đây */}
            <td className="actions-cell">
                <div className="action-buttons">
                    <button className="icon-btn" title="Sửa"><Edit size={16} /></button>
                    <button className="icon-btn danger" title="Xóa"><Trash2 size={16} /></button>
                    <button className="icon-btn"><MoreHorizontal size={16} /></button>
                </div>
            </td>
        </tr>
    );
};
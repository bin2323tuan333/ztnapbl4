import { ExternalLink } from 'lucide-react';
import type { Resource } from '../api/resourceApi';
import { ToggleSwitch } from './ToggleSwitch';
import { ResourceIcon } from './ResourceIcon';

interface Props {
    app: Resource;
    onToggle: () => void;
    onRowClick: () => void;
}

export const ResourceRow = ({ app, onToggle, onRowClick }: Props) => {
    return (
        <tr className="resource-row" onClick={onRowClick}>
            <td>
                <span style={{ color: app.enabled ? '#16a34a' : '#94a3b8', fontWeight: 500, fontSize: '14px' }}>
                    {app.enabled ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>
                <div className="app-name-cell">
                    <ResourceIcon type={app.type} />
                    <span className="app-name">{app.name}</span>
                    <ExternalLink size={14} className="external-icon" />
                </div>
            </td>
            <td className="col-type">{app.type}</td>
            <td className="col-domain">{app.host}</td>
            <td className="col-port">{app.port}</td>
            <td className="text-muted">{app.connectorZone || 'Default Zone'}</td>
            <td style={{ textAlign: 'center' }}>
                <div style={{ display: 'flex', justifyContent: 'center' }}>
                    <ToggleSwitch checked={app.enabled} onChange={onToggle} />
                </div>
            </td>
        </tr>
    );
};
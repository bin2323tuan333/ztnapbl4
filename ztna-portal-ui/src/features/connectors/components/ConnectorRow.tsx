import { Edit, Trash2, Eye, ShieldAlert, CheckCircle2 } from 'lucide-react';
import type { ConnectorSummary } from '../api/connectorApi';
import { ToggleSwitch } from './ToggleSwitch';

interface Props {
    connector: ConnectorSummary;
    onToggle: () => void;
    onView: (id: string) => void;
    onEdit: (id: string) => void;
    onDelete: (id: string) => void;
}

export const ConnectorRow = ({ connector, onToggle, onView, onEdit, onDelete }: Props) => {
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    };

    return (
        <tr className="table-row">
            <td>
                {connector.enabled ? (
                    <span className="badge active"><CheckCircle2 size={14} /> Active</span>
                ) : (
                    <span className="badge inactive"><ShieldAlert size={14} /> Inactive</span>
                )}
            </td>
            <td>
                <span className="font-medium">{connector.name}</span>
            </td>
            <td className="text-muted">{formatDate(connector.createdAt)}</td>
            <td>
                <div className="actions-cell">
                    <button className="icon-btn" title="Xem chi tiết" onClick={() => onView(connector.id)} style={{ color: '#64748b' }}>
                        <Eye size={18} />
                    </button>
                    <button className="icon-btn" title="Chỉnh sửa" onClick={() => onEdit(connector.id)} style={{ color: '#3b82f6' }}>
                        <Edit size={18} />
                    </button>
                    <button className="icon-btn danger" title="Xóa" onClick={() => onDelete(connector.id)} style={{ color: '#ef4444' }}>
                        <Trash2 size={18} />
                    </button>
                </div>
            </td>
            <td style={{ textAlign: 'center' }}>
                <ToggleSwitch enabled={connector.enabled} onChange={onToggle} />
            </td>
        </tr>
    );
};
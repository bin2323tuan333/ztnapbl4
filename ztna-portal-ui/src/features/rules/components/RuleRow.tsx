import { Trash2 } from 'lucide-react';
import type { RuleSummary } from '../api/ruleApi';
import { ToggleSwitch } from './ToggleSwitch';

interface Props {
    rule: RuleSummary;
    onToggle: () => void;
    onView: () => void;
    onDelete: (id: string, name: string) => void;
}

export const RuleRow = ({ rule, onToggle, onView, onDelete }: Props) => {
    const formatDate = (dateString: string | null) => {
        if (!dateString) return 'Vô thời hạn';
        return new Date(dateString).toLocaleString('vi-VN', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
    };

    return (
        <tr className="table-row" onClick={onView} style={{ cursor: 'pointer' }}>
            {/* 1. Tên Rule */}
            <td className="font-medium" style={{ color: '#3b82f6' }}>
                {rule.name}
            </td>

            {/* 2. Ngày tạo */}
            <td className="text-muted">
                {formatDate(rule.createdAt)}
            </td>

            {/* 3. Ngày hết hạn */}
            <td className="text-muted">
                <span style={{
                    color: rule.expiresAt ? '#ef4444' : '#64748b',
                    fontWeight: rule.expiresAt ? 500 : 400
                }}>
                    {formatDate(rule.expiresAt)}
                </span>
            </td>

            {/* 4. Bật/Tắt */}
            <td style={{ textAlign: 'center' }}>
                <div onClick={(e) => e.stopPropagation()}>
                    <ToggleSwitch enabled={rule.enabled} onChange={onToggle} />
                </div>
            </td>

            {/* Cột thao tác (chỉ giữ nút Xóa) */}
            <td style={{ textAlign: 'center', width: '80px' }}>
                <button
                    className="icon-btn danger"
                    title="Xóa Rule"
                    onClick={(e) => { e.stopPropagation(); onDelete(rule.id, rule.name); }}
                    style={{ color: '#ef4444' }}
                >
                    <Trash2 size={18} />
                </button>
            </td>
        </tr>
    );
};
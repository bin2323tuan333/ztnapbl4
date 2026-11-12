import { useEffect, useState } from 'react';
import { Trash2 } from 'lucide-react';
import type { RuleSummary } from '../api/ruleApi';
import { ToggleSwitch } from './ToggleSwitch';

interface Props {
    rule: RuleSummary;
    onToggle: () => void;
    onView: () => void;
    onDelete: (id: string, name: string) => void;
}

// setTimeout chỉ nhận tối đa ~24,8 ngày
const MAX_TIMEOUT = 2_147_483_647;

const isExpiredNow = (expiresAt: string | null) =>
    expiresAt !== null && new Date(expiresAt).getTime() <= Date.now();

export const RuleRow = ({ rule, onToggle, onView, onDelete }: Props) => {
    // Rule đã hết hạn chưa. Hẹn giờ đúng lúc hết hạn để tự đổi giao diện, không cần F5
    const [expired, setExpired] = useState(() => isExpiredNow(rule.expiresAt));

    useEffect(() => {
        setExpired(isExpiredNow(rule.expiresAt));
        if (!rule.expiresAt) return;
        const delay = new Date(rule.expiresAt).getTime() - Date.now();
        if (delay <= 0 || delay > MAX_TIMEOUT) return;
        const timer = setTimeout(() => setExpired(true), delay);
        return () => clearTimeout(timer);
    }, [rule.expiresAt]);

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

            {/* 3. Thời hạn: hết hạn rồi thì ghi "Hết hạn" thay cho ngày giờ */}
            <td className="text-muted">
                {expired ? (
                    <span style={{
                        color: '#991b1b', background: '#fee2e2', fontWeight: 600,
                        padding: '2px 10px', borderRadius: 999, fontSize: 13
                    }}>
                        Hết hạn
                    </span>
                ) : (
                    <span style={{
                        color: rule.expiresAt ? '#ef4444' : '#64748b',
                        fontWeight: rule.expiresAt ? 500 : 400
                    }}>
                        {formatDate(rule.expiresAt)}
                    </span>
                )}
            </td>

            {/* 4. Bật/Tắt: rule hết hạn thì ẩn công tắc (bật lại cũng không có tác dụng) */}
            <td style={{ textAlign: 'center' }}>
                {expired ? (
                    <span style={{ color: '#9ca3af' }}>—</span>
                ) : (
                    <div onClick={(e) => e.stopPropagation()}>
                        <ToggleSwitch enabled={rule.enabled} onChange={onToggle} />
                    </div>
                )}
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

import { useState, type FormEvent, type CSSProperties } from 'react';
import { X, Shield, AlertCircle, Clock } from 'lucide-react';
import { ruleApi } from '../api/ruleApi';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const NAME_MAX = 100;
const DESC_MAX = 255;

const toLocalDateTimeInput = (d: Date): string => {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// ─── CSS CHUẨN HÓA ───
const labelStyle: CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '13px',
    fontWeight: 500,
    marginBottom: '6px',
    color: '#334155'
};

const inputStyle: CSSProperties = {
    width: '100%',
    height: '42px',
    padding: '0 12px',
    border: '1px solid #e2e8f0',
    borderRadius: '8px',
    fontSize: '14px',
    outline: 'none',
    boxSizing: 'border-box',
    fontFamily: 'inherit',
    background: '#fff'
};

// Chỉ dùng cho khối Kích hoạt (Checkbox)
const sectionBoxStyle: CSSProperties = {
    marginBottom: '16px',
    padding: '16px',
    background: '#fff',
    border: '1px solid #e2e8f0',
    borderRadius: '8px',
};

const quickOptions = [
    { label: 'Vô thời hạn', value: 'unlimited' },
    { label: '7 ngày', value: '7d' },
    { label: '1 tháng', value: '1m' },
    { label: '3 tháng', value: '3m' },
    { label: '6 tháng', value: '6m' },
    { label: '1 năm', value: '1y' },
];

export const CreateRuleModal = ({ isOpen, onClose, onSuccess }: Props) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [enabled, setEnabled] = useState(true);
    const [expiresAt, setExpiresAt] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');

    if (!isOpen) return null;

    const resetForm = () => {
        setName('');
        setDescription('');
        setEnabled(true);
        setExpiresAt('');
        setError('');
    };

    const handleClose = () => {
        if (isSubmitting) return;
        resetForm();
        onClose();
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

        const trimmedName = name.trim();
        const trimmedDesc = description.trim();

        if (!trimmedName) { setError('Vui lòng nhập tên Rule'); return; }
        if (trimmedName.length > NAME_MAX) { setError(`Tên Rule không được vượt quá ${NAME_MAX} ký tự`); return; }
        if (trimmedDesc.length > DESC_MAX) { setError(`Mô tả không được vượt quá ${DESC_MAX} ký tự`); return; }

        let expiresIso: string | undefined;
        if (expiresAt) {
            const untilDate = new Date(expiresAt);
            if (isNaN(untilDate.getTime())) {
                setError('Ngày hết hạn không hợp lệ');
                return;
            }
            // Kiểm tra ngày hết hạn phải ở trong tương lai
            if (untilDate.getTime() <= Date.now()) {
                setError('Ngày hết hạn phải ở trong tương lai');
                return;
            }
            expiresIso = untilDate.toISOString();
        }

        setIsSubmitting(true);
        setError('');
        try {
            await ruleApi.createRule({
                name: trimmedName,
                description: trimmedDesc || undefined,
                enabled,
                expiresAt: expiresIso,
            });
            resetForm();
            onSuccess();
            onClose();
        } catch (err: any) {
            setError(err?.response?.data?.message || err?.message || 'Tạo Rule thất bại!');
        } finally {
            setIsSubmitting(false);
        }
    };

    const applyQuickExpires = (value: string) => {
        if (value === 'unlimited') {
            setExpiresAt('');
            return;
        }

        // Tính mốc thời gian từ hiện tại
        const base = new Date();
        switch (value) {
            case '7d': base.setDate(base.getDate() + 7); break;
            case '1m': base.setMonth(base.getMonth() + 1); break;
            case '3m': base.setMonth(base.getMonth() + 3); break;
            case '6m': base.setMonth(base.getMonth() + 6); break;
            case '1y': base.setFullYear(base.getFullYear() + 1); break;
        }
        setExpiresAt(toLocalDateTimeInput(base));
    };

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="form-modal-content" style={{ maxWidth: '520px' }} onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div className="modal-title-icon" style={{ background: '#eff6ff', color: '#3b82f6' }}>
                            <Shield size={22} />
                        </div>
                        <div>
                            <h2>Tạo Rule mới</h2>
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={handleClose}><X size={20} /></button>
                </div>

                <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
                    {/* Tên Rule */}
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ ...labelStyle, justifyContent: 'space-between' }}>
                            <span>Tên Rule <span style={{ color: '#ef4444' }}>*</span></span>
                            <span style={{ fontWeight: 400, color: '#94a3b8' }}>{name.length}/{NAME_MAX}</span>
                        </label>
                        <input
                            type="text"
                            style={inputStyle}
                            placeholder="VD: Dev Team Access"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            disabled={isSubmitting}
                            maxLength={NAME_MAX}
                            autoFocus
                        />
                    </div>

                    {/* Mô tả */}
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ ...labelStyle, justifyContent: 'space-between' }}>
                            <span>Mô tả</span>
                            <span style={{ fontWeight: 400, color: '#94a3b8' }}>{description.length}/{DESC_MAX}</span>
                        </label>
                        <textarea
                            rows={3}
                            style={{ ...inputStyle, height: 'auto', padding: '10px 12px', resize: 'vertical' }}
                            placeholder="Mô tả mục đích của Rule này..."
                            value={description}
                            onChange={e => setDescription(e.target.value)}
                            disabled={isSubmitting}
                            maxLength={DESC_MAX}
                        />
                    </div>

                    {/* Hạn sử dụng */}
                    <div style={{ marginBottom: '16px' }}>
                        <label style={labelStyle}>
                            <Clock size={16} /> Hạn sử dụng
                        </label>

                        <input
                            type="datetime-local"
                            value={expiresAt}
                            onChange={e => setExpiresAt(e.target.value)}
                            disabled={isSubmitting}
                            style={inputStyle}
                        />

                        {/* Preset nhanh */}
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '10px' }}>
                            {quickOptions.map(opt => {
                                const isUnlimitedActive = opt.value === 'unlimited' && !expiresAt;
                                return (
                                    <button
                                        key={opt.value}
                                        type="button"
                                        onClick={() => applyQuickExpires(opt.value)}
                                        disabled={isSubmitting}
                                        style={{
                                            fontSize: '12px', padding: '6px 12px', borderRadius: '6px',
                                            border: `1px solid ${isUnlimitedActive ? '#0f172a' : '#e2e8f0'}`,
                                            background: isUnlimitedActive ? '#0f172a' : '#fff',
                                            color: isUnlimitedActive ? '#fff' : '#475569',
                                            cursor: isSubmitting ? 'not-allowed' : 'pointer',
                                            fontWeight: 500,
                                            transition: 'all 0.2s ease'
                                        }}
                                    >
                                        {opt.label}
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* Kích hoạt */}
                    <div style={sectionBoxStyle}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: isSubmitting ? 'not-allowed' : 'pointer' }}>
                            <input
                                type="checkbox"
                                checked={enabled}
                                onChange={e => setEnabled(e.target.checked)}
                                disabled={isSubmitting}
                                style={{ width: '18px', height: '18px', cursor: 'inherit', accentColor: '#0f172a', margin: 0 }}
                            />
                            <div style={{ fontSize: '14px', fontWeight: 600, color: '#334155' }}>
                                Kích hoạt ngay sau khi tạo
                            </div>
                        </label>
                    </div>

                    {error && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#fef2f2', color: '#b91c1c', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' }}>
                            <AlertCircle size={16} /> {error}
                        </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                        <button type="button" className="btn-outline" onClick={handleClose} disabled={isSubmitting}
                            style={{ padding: '10px 20px', border: '1px solid #e2e8f0', background: '#fff', borderRadius: '8px', cursor: isSubmitting ? 'not-allowed' : 'pointer', fontWeight: 500 }}>
                            Hủy
                        </button>
                        <button type="submit" className="btn-black" disabled={isSubmitting}
                            style={{ padding: '10px 20px', background: '#0f172a', color: '#fff', border: 'none', borderRadius: '8px', cursor: isSubmitting ? 'not-allowed' : 'pointer', fontWeight: 500 }}>
                            {isSubmitting ? 'Đang tạo...' : 'Tạo Rule'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
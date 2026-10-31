import { useState, type FormEvent } from 'react';
import { X, Shield, AlertCircle } from 'lucide-react';
import { ruleApi } from '../api/ruleApi';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

export const CreateRuleModal = ({ isOpen, onClose, onSuccess }: Props) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');

    if (!isOpen) return null;

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!name.trim()) {
            setError('Vui lòng nhập tên Rule');
            return;
        }

        setIsSubmitting(true);
        setError('');
        try {
            await ruleApi.createRule({ name: name.trim(), description: description.trim() });
            setName(''); // Reset form
            setDescription('');
            onSuccess();
            onClose();
        } catch (err: any) {
            setError(err.message || 'Tạo Rule thất bại!');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="form-modal-content" style={{ maxWidth: '500px' }} onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div className="modal-title-icon" style={{ background: '#eff6ff', color: '#3b82f6' }}>
                            <Shield size={22} />
                        </div>
                        <div>
                            <h2>Tạo Rule mới</h2>
                            <p style={{ margin: 0, fontSize: '13px', color: '#64748b' }}>Tạo nhóm quy tắc truy cập</p>
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>

                <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 500, marginBottom: '6px', color: '#334155' }}>Tên Rule *</label>
                        <input
                            type="text"
                            className="search-box-input"
                            style={{ width: '100%', padding: '10px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '14px', outline: 'none' }}
                            placeholder="VD: Dev Team Access"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            disabled={isSubmitting}
                            autoFocus
                        />
                    </div>

                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 500, marginBottom: '6px', color: '#334155' }}>Mô tả</label>
                        <textarea
                            rows={3}
                            style={{ width: '100%', padding: '10px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '14px', outline: 'none', resize: 'vertical' }}
                            placeholder="Mô tả mục đích của Rule này..."
                            value={description}
                            onChange={e => setDescription(e.target.value)}
                            disabled={isSubmitting}
                        />
                    </div>

                    {error && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px', background: '#fef2f2', color: '#b91c1c', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' }}>
                            <AlertCircle size={16} /> {error}
                        </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '24px' }}>
                        <button type="button" className="btn-outline" onClick={onClose} disabled={isSubmitting} style={{ padding: '8px 16px', border: '1px solid #e2e8f0', background: '#fff', borderRadius: '8px', cursor: 'pointer' }}>
                            Hủy
                        </button>
                        <button type="submit" className="btn-black" disabled={isSubmitting} style={{ padding: '8px 16px', background: '#0f172a', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer' }}>
                            {isSubmitting ? 'Đang tạo...' : 'Tạo Rule'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
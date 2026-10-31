import { useState, useEffect, type FormEvent } from 'react';
import { X, Server, AlertCircle, Loader2 } from 'lucide-react';
import { connectorApi, type ConnectorDetail } from '../api/connectorApi';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    connectorId: string | null;
}

export const ConnectorFormModal = ({ isOpen, onClose, onSuccess, connectorId }: Props) => {
    const [formData, setFormData] = useState({
        name: '',
        connectionString: '',
        enabled: true
    });
    const [isLoadingData, setIsLoadingData] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');

    useEffect(() => {
        if (!isOpen) return;
        let isMounted = true;
        setSubmitError('');

        const loadData = async () => {
            if (connectorId) {
                setIsLoadingData(true);
                try {
                    const data = await connectorApi.getConnectorById(connectorId);
                    if (isMounted) {
                        setFormData({
                            name: data.name,
                            connectionString: data.connectionString,
                            enabled: data.enabled
                        });
                    }
                } catch (err: any) {
                    if (isMounted) setSubmitError(err.message || 'Không thể tải dữ liệu');
                } finally {
                    if (isMounted) setIsLoadingData(false);
                }
            } else {
                setFormData({ name: '', connectionString: '', enabled: true });
            }
        };

        loadData();
        return () => { isMounted = false; };
    }, [isOpen, connectorId]);

    if (!isOpen) return null;

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim() || !formData.connectionString.trim()) {
            setSubmitError('Vui lòng nhập đầy đủ Tên và Connection String.');
            return;
        }

        setIsSubmitting(true);
        setSubmitError('');

        try {
            const payload = {
                name: formData.name.trim(),
                connectionString: formData.connectionString.trim(),
                enabled: formData.enabled
            };

            if (connectorId) {
                await connectorApi.updateConnector(connectorId, payload);
            } else {
                await connectorApi.createConnector(payload);
            }
            onSuccess();
            onClose();
        } catch (err: any) {
            setSubmitError(err?.message || 'Lưu thất bại, vui lòng thử lại.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="form-modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <div className="modal-title-group">
                        <div className="modal-title-icon" style={{ background: '#f5f3ff', color: '#7c3aed' }}>
                            <Server size={22} />
                        </div>
                        <div>
                            <h2>{connectorId ? 'Edit Connector' : 'New Connector'}</h2>
                            <p className="modal-subtitle">Cấu hình Agent kết nối với hệ thống</p>
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>

                {isLoadingData ? (
                    <div style={{ padding: '60px 0', textAlign: 'center', color: '#64748b' }}>
                        <Loader2 size={32} className="spin" style={{ margin: '0 auto 12px' }} />
                        <p>Đang tải dữ liệu...</p>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit} className="form-body">
                        <div className="form-group">
                            <label className="field-label">Connector Name *</label>
                            <input
                                required
                                className="field-input"
                                placeholder="VD: Local Agent Demo"
                                value={formData.name}
                                onChange={e => setFormData({ ...formData, name: e.target.value })}
                                disabled={isSubmitting}
                            />
                        </div>

                        <div className="form-group">
                            <label className="field-label">Connection String *</label>
                            <textarea
                                required
                                rows={4}
                                className="field-input field-textarea font-mono"
                                placeholder="VD: tcp://0.0.0.0:8080?token=..."
                                value={formData.connectionString}
                                onChange={e => setFormData({ ...formData, connectionString: e.target.value })}
                                disabled={isSubmitting}
                            />
                        </div>

                        {connectorId && (
                            <div className="form-group">
                                <label className="field-label">Status</label>
                                <div className="toggle-row">
                                    <button
                                        type="button"
                                        role="switch"
                                        aria-checked={formData.enabled}
                                        className={`switch ${formData.enabled ? 'on' : 'off'}`}
                                        onClick={() => setFormData(prev => ({ ...prev, enabled: !prev.enabled }))}
                                        disabled={isSubmitting}
                                    >
                                        <span className="switch-knob" />
                                    </button>
                                    <span className="toggle-label">
                                        {formData.enabled ? 'Active' : 'Inactive'}
                                    </span>
                                </div>
                            </div>
                        )}

                        {submitError && (
                            <div className="form-error">
                                <AlertCircle size={16} />
                                <span>{submitError}</span>
                            </div>
                        )}

                        <div className="form-footer">
                            <button type="button" className="btn-outline" onClick={onClose} disabled={isSubmitting}>
                                Cancel
                            </button>
                            <button type="submit" className="btn-black" disabled={isSubmitting}>
                                {isSubmitting ? 'Saving...' : (connectorId ? 'Save Changes' : 'Create Connector')}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
};
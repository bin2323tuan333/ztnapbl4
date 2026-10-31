import { useState, useEffect, type FormEvent } from 'react';
import {
    X, Globe, Terminal, Database, Server, Check, AlertCircle, Loader2, type LucideIcon,
} from 'lucide-react';
import {
    resourceApi, type ResourceType, type ConnectorsName,
} from '../api/resourceApi';
import '../styles/ResourceFormModal.css';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    resourceId: string | null; // Nhận ID thay vì Object
    defaultType?: ResourceType;
}

interface FormState {
    name: string;
    type: ResourceType;
    host: string;
    port: number;
    description: string;
    connectorId: string;
    enabled: boolean;
}

const TYPE_OPTIONS: {
    value: ResourceType;
    label: string;
    icon: LucideIcon;
    defaultPort: number;
    hint: string;
}[] = [
        { value: 'WEB', label: 'Web', icon: Globe, defaultPort: 80, hint: 'HTTP / HTTPS' },
        { value: 'SSH', label: 'SSH', icon: Terminal, defaultPort: 22, hint: 'Secure Shell' },
        { value: 'MYSQL', label: 'MySQL', icon: Database, defaultPort: 3306, hint: 'Database' },
    ];

const buildEmpty = (type: ResourceType = 'WEB'): FormState => ({
    name: '',
    type,
    host: '',
    port: TYPE_OPTIONS.find(t => t.value === type)?.defaultPort ?? 80,
    description: '',
    connectorId: '',
    enabled: true,
});

export const ResourceFormModal = ({
    isOpen, onClose, onSuccess, resourceId, defaultType,
}: Props) => {
    const [formData, setFormData] = useState<FormState>(buildEmpty(defaultType));
    const [connectors, setConnectors] = useState<ConnectorsName[]>([]);

    // State để kiểm tra xem trạng thái enabled ban đầu là gì (phục vụ cho việc gọi API toggle)
    const [originalEnabled, setOriginalEnabled] = useState(true);

    // State loading tổng (chờ load xong cả Resource + Connectors)
    const [isLoadingData, setIsLoadingData] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');

    // Load dữ liệu mỗi khi mở form
    useEffect(() => {
        if (!isOpen) return;

        let isMounted = true;

        const loadData = async () => {
            setIsLoadingData(true);
            setSubmitError('');

            try {
                // Gọi song song cả 2 API để tối ưu tốc độ và tránh lỗi bất đồng bộ
                const [fetchedConnectors, fetchedResource] = await Promise.all([
                    resourceApi.getConnectors(),
                    resourceId ? resourceApi.getResourceDetail(resourceId) : Promise.resolve(null)
                ]);

                if (!isMounted) return;

                // 1. Lưu danh sách Connectors
                setConnectors(fetchedConnectors);

                // 2. Khởi tạo FormData
                if (fetchedResource) {
                    setFormData({
                        name: fetchedResource.name,
                        type: fetchedResource.type,
                        host: fetchedResource.host,
                        port: fetchedResource.port,
                        description: fetchedResource.description || '',
                        connectorId: fetchedResource.connectorId || '', // Map chính xác ID
                        enabled: fetchedResource.enabled ?? true,
                    });
                    setOriginalEnabled(fetchedResource.enabled ?? true);
                } else {
                    setFormData(buildEmpty(defaultType));
                    setOriginalEnabled(true);
                }
            } catch (err: any) {
                if (isMounted) setSubmitError(err.message || 'Lỗi tải dữ liệu.');
            } finally {
                if (isMounted) setIsLoadingData(false);
            }
        };

        loadData();

        return () => { isMounted = false; };
    }, [isOpen, resourceId, defaultType]);

    if (!isOpen) return null;

    const ActiveIcon = TYPE_OPTIONS.find(t => t.value === formData.type)?.icon ?? Server;

    // Tự động đổi port mặc định khi đổi Type (chỉ khi user chưa tự nhập port khác)
    const handleTypeChange = (newType: ResourceType) => {
        const newDefault = TYPE_OPTIONS.find(t => t.value === newType)?.defaultPort;
        const wasDefault = TYPE_OPTIONS.some(t => t.defaultPort === formData.port);
        setFormData(prev => ({
            ...prev,
            type: newType,
            port: wasDefault && newDefault ? newDefault : prev.port,
        }));
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setSubmitError('');

        if (formData.port < 1 || formData.port > 65535) {
            setSubmitError('Port phải nằm trong khoảng 1 – 65535.');
            return;
        }
        if (!formData.name.trim() || !formData.host.trim()) {
            setSubmitError('Vui lòng nhập đầy đủ Name và Host.');
            return;
        }

        setIsSubmitting(true);
        try {
            const payload = {
                name: formData.name.trim(),
                type: formData.type,
                host: formData.host.trim(),
                port: formData.port,
                description: formData.description.trim() || undefined,
                connectorId: formData.connectorId || undefined,
            };

            if (resourceId) {
                await resourceApi.updateResource(resourceId, payload);
                // Đồng bộ trạng thái Active/Inactive nếu có thay đổi
                if (originalEnabled !== formData.enabled) {
                    await resourceApi.toggleStatus(resourceId, formData.enabled);
                }
            } else {
                await resourceApi.createResource(payload);
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
                {/* Header */}
                <div className="modal-header">
                    <div className="modal-title-group">
                        <div className="modal-title-icon" data-type={formData.type}>
                            <ActiveIcon size={22} />
                        </div>
                        <div>
                            <h2>{resourceId ? 'Edit Application' : 'New Application'}</h2>
                            <p className="modal-subtitle">
                                {resourceId
                                    ? 'Cập nhật cấu hình tài nguyên'
                                    : 'Đăng ký tài nguyên mới vào hệ thống ZTNA'}
                            </p>
                        </div>
                    </div>
                    <button className="modal-close-btn" onClick={onClose} aria-label="Close">
                        <X size={20} />
                    </button>
                </div>

                {/* Hiển thị loading nếu đang tải data (tránh render form khi chưa có connectors) */}
                {isLoadingData ? (
                    <div style={{ padding: '60px 0', textAlign: 'center', color: '#64748b' }}>
                        <Loader2 size={32} className="spin" style={{ margin: '0 auto 12px' }} />
                        <p>Đang tải dữ liệu...</p>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit} className="form-body">
                        {/* Type Selector */}
                        <div className="form-group">
                            <label className="field-label">Resource Type *</label>
                            <div className="type-grid">
                                {TYPE_OPTIONS.map(opt => {
                                    const Icon = opt.icon;
                                    const active = formData.type === opt.value;
                                    return (
                                        <button
                                            key={opt.value}
                                            type="button"
                                            className={`type-card ${active ? 'active' : ''}`}
                                            onClick={() => handleTypeChange(opt.value)}
                                            disabled={isSubmitting}
                                        >
                                            <div className="type-icon"><Icon size={18} /></div>
                                            <div>
                                                <span className="type-label">{opt.label}</span>
                                                <span className="type-hint">{opt.hint}</span>
                                            </div>
                                            {active && (
                                                <div className="type-check"><Check size={12} /></div>
                                            )}
                                        </button>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Name */}
                        <div className="form-group">
                            <label className="field-label">Name *</label>
                            <input
                                required
                                maxLength={100}
                                className="field-input"
                                placeholder="VD: My Production App"
                                value={formData.name}
                                onChange={e => setFormData({ ...formData, name: e.target.value })}
                                disabled={isSubmitting}
                            />
                        </div>

                        {/* Host + Port */}
                        <div className="form-row">
                            <div className="form-group" style={{ flex: 2 }}>
                                <label className="field-label">Host / IP *</label>
                                <input
                                    required
                                    className="field-input"
                                    placeholder="VD: 192.168.1.10 hoặc db.example.com"
                                    value={formData.host}
                                    onChange={e => setFormData({ ...formData, host: e.target.value })}
                                    disabled={isSubmitting}
                                />
                            </div>
                            <div className="form-group" style={{ flex: 1 }}>
                                <label className="field-label">Port *</label>
                                <input
                                    required
                                    type="number"
                                    min={1}
                                    max={65535}
                                    className="field-input"
                                    value={formData.port}
                                    onChange={e =>
                                        setFormData({ ...formData, port: Number(e.target.value) })
                                    }
                                    disabled={isSubmitting}
                                />
                            </div>
                        </div>

                        {/* Connector Zone */}
                        <div className="form-group">
                            <label className="field-label">Connector Zone</label>
                            <div className="select-wrapper">
                                <select
                                    className="field-input"
                                    value={formData.connectorId}
                                    onChange={e =>
                                        setFormData({ ...formData, connectorId: e.target.value })
                                    }
                                    disabled={isSubmitting}
                                >
                                    <option value="">— Không chọn (Default) —</option>
                                    {connectors.map(c => (
                                        <option key={c.id} value={c.id}>{c.name}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Description */}
                        <div className="form-group">
                            <label className="field-label">Description</label>
                            <textarea
                                rows={3}
                                maxLength={500}
                                className="field-input field-textarea"
                                placeholder="Mô tả ngắn về mục đích sử dụng…"
                                value={formData.description}
                                onChange={e =>
                                    setFormData({ ...formData, description: e.target.value })
                                }
                                disabled={isSubmitting}
                            />
                        </div>

                        {/* Enabled Switch (Chỉ hiển thị khi Edit) */}
                        {resourceId && (
                            <div className="form-group">
                                <label className="field-label">Status</label>
                                <div className="toggle-row">
                                    <button
                                        type="button"
                                        role="switch"
                                        aria-checked={formData.enabled}
                                        className={`switch ${formData.enabled ? 'on' : 'off'}`}
                                        onClick={() =>
                                            setFormData(prev => ({ ...prev, enabled: !prev.enabled }))
                                        }
                                        disabled={isSubmitting}
                                    >
                                        <span className="switch-knob" />
                                    </button>
                                    <span className="toggle-label">
                                        {formData.enabled
                                            ? 'Active — người dùng có thể truy cập'
                                            : 'Inactive — tạm khoá truy cập'}
                                    </span>
                                </div>
                            </div>
                        )}

                        {/* Error Banner */}
                        {submitError && (
                            <div className="form-error">
                                <AlertCircle size={16} />
                                <span>{submitError}</span>
                            </div>
                        )}

                        {/* Footer */}
                        <div className="form-footer">
                            <button
                                type="button"
                                className="btn-outline"
                                onClick={onClose}
                                disabled={isSubmitting}
                            >
                                Cancel
                            </button>
                            <button type="submit" className="btn-black" disabled={isSubmitting}>
                                {isSubmitting ? (
                                    <><Loader2 size={16} className="spin" /> Saving…</>
                                ) : (
                                    resourceId ? 'Save Changes' : 'Create Application'
                                )}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
};
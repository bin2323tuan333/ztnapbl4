import { useState, useEffect } from 'react';
import { X, Search } from 'lucide-react';
import type { UserInfo, ResourceInfo } from '../api/ruleApi';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSave: (selectedIds: string[]) => Promise<void>;
    title: string;
    allItems: (UserInfo | ResourceInfo)[];
    initialSelectedIds: string[];
    itemType: 'user' | 'resource';
}

export const EditRuleAssignmentModal = ({ isOpen, onClose, onSave, title, allItems, initialSelectedIds }: Props) => {
    const [selectedIds, setSelectedIds] = useState<string[]>([]);
    const [searchLeft, setSearchLeft] = useState('');
    const [searchRight, setSearchRight] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setSelectedIds(initialSelectedIds);
            setSearchLeft('');
            setSearchRight('');
        }
    }, [isOpen, initialSelectedIds]);

    if (!isOpen) return null;

    const toggleSelection = (id: string) => {
        setSelectedIds(prev =>
            prev.includes(id) ? prev.filter(item => item !== id) : [...prev, id]
        );
    };

    const handleSave = async () => {
        setIsSaving(true);
        try {
            await onSave(selectedIds);
            onClose();
        } catch (err) {
            alert('Có lỗi xảy ra khi lưu!');
        } finally {
            setIsSaving(false);
        }
    };

    // ── Render nội dung giống RuleDetailModal ──
    const renderItemContent = (item: UserInfo | ResourceInfo) => {
        if ('username' in item) {
            return (
                <>
                    <span style={{ fontWeight: 500 }}>{item.username}</span>{' '}
                    <span style={{ color: '#94a3b8', fontSize: '12px' }}>({item.email})</span>
                </>
            );
        }
        return (
            <>
                <span style={{ fontWeight: 500 }}>{item.name}</span>{' '}
                <span style={{ color: '#94a3b8', fontSize: '12px' }}>[{item.type}]</span>
            </>
        );
    };

    // Tìm theo cả username/name + email/type (vì đã hiển thị các field này)
    const getSearchText = (item: UserInfo | ResourceInfo) =>
        'username' in item
            ? `${item.username} ${item.email ?? ''}`.toLowerCase()
            : `${item.name} ${item.type ?? ''}`.toLowerCase();

    const selectedItems = allItems
        .filter(item => selectedIds.includes(item.id))
        .filter(item => getSearchText(item).includes(searchLeft.toLowerCase()));

    const availableItems = allItems
        .filter(item => !selectedIds.includes(item.id))
        .filter(item => getSearchText(item).includes(searchRight.toLowerCase()));

    // Style chung cho hàng, đồng bộ với detail
    const rowStyle: React.CSSProperties = {
        padding: '10px',
        background: '#f8fafc',
        border: '1px solid #e2e8f0',
        borderRadius: '6px',
        fontSize: '14px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
    };

    const textStyle: React.CSSProperties = {
        flex: 1,
        minWidth: 0,
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    };

    return (
        <div className="modal-overlay" style={{ zIndex: 1100 }} onClick={onClose}>
            <div className="form-modal-content" style={{ maxWidth: '850px' }} onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>{title}</h2>
                    <button className="modal-close-btn" onClick={onClose}><X size={20} /></button>
                </div>

                <div className="transfer-container">
                    {/* CỘT TRÁI: ĐÃ CHỌN */}
                    <div className="transfer-panel">
                        <div className="panel-search">
                            <Search size={16} className="search-icon" />
                            <input type="text" placeholder="Tìm trong đã chọn..." value={searchLeft} onChange={e => setSearchLeft(e.target.value)} />
                        </div>
                        <div className="panel-list">
                            {selectedItems.map(item => (
                                <div
                                    key={item.id}
                                    style={rowStyle}
                                    onClick={() => toggleSelection(item.id)}
                                    title="Bấm để bỏ chọn"
                                >
                                    <span style={textStyle}>{renderItemContent(item)}</span>
                                    <X size={16} style={{ color: '#ef4444', flexShrink: 0 }} />
                                </div>
                            ))}
                            {selectedItems.length === 0 && <div className="empty-text">Chưa có dữ liệu.</div>}
                        </div>
                    </div>

                    {/* CỘT PHẢI: CÓ SẴN */}
                    <div className="transfer-panel">
                        <div className="panel-search">
                            <Search size={16} className="search-icon" />
                            <input type="text" placeholder="Tìm kiếm để thêm..." value={searchRight} onChange={e => setSearchRight(e.target.value)} />
                        </div>
                        <div className="panel-list">
                            {availableItems.map(item => (
                                <div
                                    key={item.id}
                                    style={rowStyle}
                                    onClick={() => toggleSelection(item.id)}
                                    title="Bấm để chọn"
                                >
                                    <span style={textStyle}>{renderItemContent(item)}</span>
                                </div>
                            ))}
                            {availableItems.length === 0 && <div className="empty-text">Không tìm thấy kết quả.</div>}
                        </div>
                    </div>
                </div>

                <div className="form-footer" style={{ padding: '16px 24px', borderTop: '1px solid #e2e8f0', display: 'flex', justifyContent: 'flex-end', background: '#fff' }}>
                    <button className="btn-black" onClick={handleSave} disabled={isSaving} style={{ width: '120px' }}>
                        {isSaving ? 'Đang lưu...' : 'Lưu lại'}
                    </button>
                </div>
            </div>
        </div>
    );
};
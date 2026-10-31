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

export const EditRuleAssignmentModal = ({ isOpen, onClose, onSave, title, allItems, initialSelectedIds, itemType }: Props) => {
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

    const getItemName = (item: UserInfo | ResourceInfo) => 'username' in item ? item.username : item.name;

    // Danh sách đã chọn (Cột Trái)
    const selectedItems = allItems
        .filter(item => selectedIds.includes(item.id))
        .filter(item => getItemName(item).toLowerCase().includes(searchLeft.toLowerCase()));

    // Danh sách có sẵn (Cột Phải)
    const availableItems = allItems
        .filter(item => !selectedIds.includes(item.id))
        .filter(item => getItemName(item).toLowerCase().includes(searchRight.toLowerCase()));

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
                                <div key={item.id} className="list-card selected-card" onClick={() => toggleSelection(item.id)}>
                                    <span>{getItemName(item)}</span>
                                    <X size={16} className="icon-remove" />
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
                                <div key={item.id} className="list-card available-card" onClick={() => toggleSelection(item.id)}>
                                    <div className="checkbox-custom"></div>
                                    <span>{getItemName(item)}</span>
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
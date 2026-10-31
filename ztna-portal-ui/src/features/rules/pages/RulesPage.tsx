import { useState } from 'react';
import { Search, Plus } from 'lucide-react'; // Thêm icon Plus
import { useRules } from '../hooks/useRules';
import '../styles/Rules.css';
import { RuleIcon } from '../components/RuleIcon';
import { RuleRow } from '../components/RuleRow';
import { RuleDetailModal } from '../components/RuleDetailModal';
import { CreateRuleModal } from '../components/CreateRuleModal';

export const RulesPage = () => {
    const { rules, isLoading, error, searchTerm, setSearchTerm, toggleStatus, deleteRule, refetch } = useRules();
    const [selectedId, setSelectedId] = useState<string | null>(null);

    // State quản lý Modal Tạo mới
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    const handleDelete = async (id: string, name: string) => {
        if (window.confirm(`Chắc chắn xóa Rule "${name}"?`)) {
            await deleteRule(id);
        }
    };

    return (
        <div className="resources-container">
            <div className="resources-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <RuleIcon size={24} />
                    <div>
                        <h1 className="resources-title">Quản lý Quy tắc (Rules)</h1>
                    </div>
                </div>
            </div>

            <div className="resources-toolbar">
                <div className="search-box">
                    <Search size={16} className="search-icon" />
                    <input
                        type="text"
                        placeholder="Tìm kiếm rule..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* NÚT TẠO RULE MỚI */}
                <div className="toolbar-actions">
                    <button className="btn-black" onClick={() => setIsCreateModalOpen(true)}>
                        <Plus size={16} /> Thêm Rule
                    </button>
                </div>
            </div>

            {error && <div className="error-msg">{error}</div>}

            <div className="resources-table-wrapper">
                <table className="resources-table">
                    <thead>
                        <tr>
                            <th>Tên Rule</th>
                            <th>Ngày tạo</th>
                            <th>Hết hạn</th>
                            <th style={{ textAlign: 'center' }}>Bật/Tắt</th>
                            <th style={{ textAlign: 'center' }}>Xóa</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={5} className="text-center py-8">Đang tải...</td></tr>
                        ) : rules.length === 0 ? (
                            <tr><td colSpan={5} className="text-center py-8">Chưa có Rule nào.</td></tr>
                        ) : (
                            rules.map(r => (
                                <RuleRow
                                    key={r.id}
                                    rule={r}
                                    onToggle={() => toggleStatus(r.id, r.enabled)}
                                    onView={() => setSelectedId(r.id)}
                                    onDelete={handleDelete}
                                />
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* MODAL CHI TIẾT */}
            <RuleDetailModal
                isOpen={!!selectedId}
                onClose={() => setSelectedId(null)}
                ruleId={selectedId}
                onRefresh={refetch}
            />

            {/* MODAL TẠO MỚI */}
            <CreateRuleModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSuccess={refetch}
            />
        </div>
    );
};
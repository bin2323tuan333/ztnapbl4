import { useMemo, useState } from 'react';
import { Monitor } from 'lucide-react';
import { usePortal } from '../hooks/usePortal';
import '../styles/PortalPage.css';
import { SearchBar } from '@/components/common/SearchBar';
import { ResourceCard } from '../component/ResourceCard';
import { AppDetailModal } from '../component/AppDetailModal';

const PortalPage = () => {
    const {
        resources,
        selectedResource,
        loading,
        handleSelectResource,
        handleCloseModal,
    } = usePortal();

    const [keyword, setKeyword] = useState('');

    const filtered = useMemo(() => {
        const q = keyword.trim().toLowerCase();
        if (!q) return resources;
        return resources.filter((r) =>
            r.name.toLowerCase().includes(q) ||
            (r.description ?? '').toLowerCase().includes(q) ||
            r.type.toLowerCase().includes(q)
        );
    }, [resources, keyword]);

    if (loading) {
        return <div className="portal-loading">Đang tải dữ liệu...</div>;
    }

    return (
        <>
            <div className="portal-wrapper">
                {resources.length === 0 ? (
                    <div className="portal-empty-state">
                        <Monitor size={64} className="portal-empty-icon" />
                        <h2 className="portal-empty-title">Chưa có ứng dụng nào</h2>
                        <p className="portal-empty-desc">
                            Hiện tại bạn chưa được cấp quyền truy cập ứng dụng nào.
                            <br />
                            Vui lòng liên hệ quản trị viên để được cấp quyền.
                        </p>
                    </div>
                ) : (
                    <>
                        <SearchBar
                            value={keyword}
                            onChange={setKeyword}
                            placeholder="Tìm theo tên, mô tả hoặc loại ứng dụng..."
                            className="portal-search"
                        />

                        {filtered.length === 0 ? (
                            <div className="portal-search-empty">
                                Không tìm thấy ứng dụng nào khớp với "<strong>{keyword}</strong>".
                            </div>
                        ) : (
                            <div className="portal-grid">
                                {filtered.map((item) => (
                                    <ResourceCard
                                        key={item.id}
                                        resource={item}
                                        onClick={handleSelectResource}
                                    />
                                ))}
                            </div>
                        )}
                    </>
                )}
            </div>

            <AppDetailModal
                isOpen={selectedResource != null}
                resource={selectedResource}
                onClose={handleCloseModal}
            />
        </>
    );
};

export default PortalPage;
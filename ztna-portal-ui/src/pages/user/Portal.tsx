import { useEffect, useState } from 'react';
import { getRequest } from '../../utils/apiClient';
import type { Resource } from '../../types/model/Resource';
import AppDetailModal from '../../components/modal/AppDetailModal';
import ResourceCard from '../../components/card/ResourceCard';

const Portal = () => {
    const [resources, setResources] = useState<Resource[]>([]);
    const [selectedResource, setSelectedResource] = useState<Resource | null>(null);
    const [loading, setLoading] = useState(true);

    // Gọi API lấy danh sách Resource khi vừa mở trang
    useEffect(() => {
        const fetchMyApps = async () => {
            try {
                setLoading(true);
                const data: Resource[] = await getRequest('/resources');
                setResources(data);
            } catch (error) {
                console.error("Lỗi khi tải danh sách ứng dụng:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchMyApps();
    }, []);

    if (loading) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>Đang tải dữ liệu...</div>;
    }

    return (
        <>
            <div style={{
                width: '100%',
                height: '100%',
                backgroundColor: '#f9fafb',
                overflowY: 'auto'
            }}>
                <div style={{ textAlign: 'center', marginBottom: '40px', paddingTop: '20px' }}>
                    <h1 style={{ fontSize: '28px', color: '#1f2937', margin: '0 0 10px 0' }}>
                        ZTNA Application Portal
                    </h1>
                    <p style={{ color: '#6b7280' }}>Chọn ứng dụng bên dưới để bắt đầu làm việc</p>
                </div>

                {resources.length === 0 ? (
                    <div style={{ textAlign: 'center', color: '#9ca3af' }}>
                        Hiện tại bạn chưa được cấp quyền truy cập ứng dụng nào.
                    </div>
                ) : (
                    <div className="resource-grid" style={{ maxWidth: '1200px', margin: '0 auto', paddingBottom: '40px' }}>
                        {resources.map(item => (
                            <ResourceCard
                                key={item.id}
                                resource={item}
                                onClick={(res) => setSelectedResource(res)}
                            />
                        ))}
                    </div>
                )}
            </div>

            <AppDetailModal
                isOpen={selectedResource !== null}
                resource={selectedResource}
                onClose={() => setSelectedResource(null)}
            />
        </>
    );
};

export default Portal;
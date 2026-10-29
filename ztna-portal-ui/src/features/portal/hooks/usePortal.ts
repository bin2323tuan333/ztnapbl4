// src/features/portal/hooks/usePortal.ts
import { useEffect, useState } from 'react';
import { portalApi } from '../api/portalApi';
import type { Resource } from '../types';

export const usePortal = () => {
    const [resources, setResources] = useState<Resource[]>([]);
    const [selectedResource, setSelectedResource] = useState<Resource | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMyApps = async () => {
            try {
                setLoading(true);
                const data = await portalApi.getMyResources();
                setResources(data);
            } catch (error) {
                console.error("Lỗi khi tải danh sách ứng dụng:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchMyApps();
    }, []);

    // Các hàm xử lý sự kiện
    const handleSelectResource = (res: Resource) => setSelectedResource(res);
    const handleCloseModal = () => setSelectedResource(null);

    return {
        resources,
        selectedResource,
        loading,
        handleSelectResource,
        handleCloseModal
    };
};
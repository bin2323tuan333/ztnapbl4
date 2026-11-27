import { getRequest } from '@/shared/utils/apiClient';
import type { Resource, ResourceType } from '../types';

export const portalApi = {
    getMyResources: async (): Promise<Resource[]> => {
        const data: any[] = (await getRequest('/api/v1/myresources')) ?? [];
        // Backend trả loại app chữ hoa ("WEB"), giao diện và Agent dùng chữ thường ("web"):
        // chuẩn hoá một lần ở đây để icon, tìm kiếm và trình mở app (launcher) của Agent đều đúng.
        return data.map((r) => ({
            ...r,
            type: String(r.type ?? 'other').toLowerCase() as ResourceType,
        }));
    }
};

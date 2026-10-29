import { getRequest } from '@/shared/utils/apiClient';
import type { Resource } from '../types';

export const portalApi = {
    getMyResources: async (): Promise<Resource[]> => {
        return await getRequest('/myresources');
    }
};
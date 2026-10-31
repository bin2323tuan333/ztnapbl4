import { getRequest, postRequest, putRequest, patchRequest, deleteRequest } from '@/shared/utils/apiClient';

export type ResourceType = 'WEB' | 'SSH' | 'MYSQL';

export interface ConnectorsName {
    id: string;
    name: string;
}

export interface Resource {
    id: string;
    name: string;
    type: ResourceType;
    host: string;
    port: number;
    enabled: boolean;
    connectorZone?: string;
}

export interface ResourceDetail extends Resource {
    description?: string;
    createdAt?: string;
    updatedAt?: string;
    connectorId?: string;
}

export interface ResourceRequest {
    name: string;
    type: ResourceType;
    host: string;
    port: number;
    description?: string;
    connectorId?: string;
}

export const resourceApi = {
    getApplications: async (): Promise<Resource[]> => {
        return await getRequest('/api/v1/admin/resources');
    },
    getResourceDetail: async (id: string): Promise<ResourceDetail> => {
        return await getRequest(`/api/v1/admin/resources/${id}`);
    },
    createResource: async (data: ResourceRequest): Promise<ResourceDetail> => {
        return await postRequest('/api/v1/admin/resources', data);
    },
    updateResource: async (id: string, data: ResourceRequest): Promise<ResourceDetail> => {
        return await putRequest(`/api/v1/admin/resources/${id}`, data);
    },
    toggleStatus: async (id: string, enabled: boolean): Promise<ResourceDetail> => {
        const action = enabled ? 'enable' : 'disable';
        return await patchRequest(`/api/v1/admin/resources/${id}/${action}`);
    },
    deleteResource: async (id: string): Promise<void> => {
        return await deleteRequest(`/api/v1/admin/resources/${id}`);
    },
    getConnectors: async (): Promise<ConnectorsName[]> => {
        return await getRequest('/api/v1/admin/connectors/names');
    }
};
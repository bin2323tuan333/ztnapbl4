import { getRequest, postRequest, putRequest, patchRequest, deleteRequest } from '@/shared/utils/apiClient';

export interface ConnectorName {
    id: string;
    name: string;
}

export interface ConnectorSummary {
    id: string;
    name: string;
    enabled: boolean;
    createdAt: string;
}

export interface ConnectorDetail {
    id: string;
    name: string;
    connectionString: string;
    enabled: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface ConnectorCreationRequest {
    name: string;
    connectionString: string;
    enabled: boolean;
}

export interface ConnectorUpdateRequest {
    name: string;
    connectionString: string;
    enabled: boolean;
}

export const connectorApi = {
    // Lấy danh sách rút gọn cho Dropdown
    getConnectorNames: async (): Promise<ConnectorName[]> => {
        return await getRequest('/api/v1/admin/connectors/names');
    },
    // Lấy danh sách cho bảng
    getConnectors: async (): Promise<ConnectorSummary[]> => {
        return await getRequest('/api/v1/admin/connectors');
    },
    // Lấy chi tiết
    getConnectorById: async (id: string): Promise<ConnectorDetail> => {
        return await getRequest(`/api/v1/admin/connectors/${id}`);
    },
    // Tạo mới
    createConnector: async (data: ConnectorCreationRequest): Promise<ConnectorDetail> => {
        return await postRequest('/api/v1/admin/connectors', data);
    },
    // Cập nhật
    updateConnector: async (id: string, data: ConnectorUpdateRequest): Promise<ConnectorDetail> => {
        return await putRequest(`/api/v1/admin/connectors/${id}`, data);
    },
    // Bật/Tắt
    toggleStatus: async (id: string): Promise<ConnectorDetail> => {
        return await patchRequest(`/api/v1/admin/connectors/${id}/toggle`);
    },
    // Xóa
    deleteConnector: async (id: string): Promise<void> => {
        return await deleteRequest(`/api/v1/admin/connectors/${id}`);
    }
};
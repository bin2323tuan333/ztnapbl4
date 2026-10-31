import { getRequest, putRequest, patchRequest, deleteRequest, postRequest } from '@/shared/utils/apiClient';

export interface UserInfo {
    id: string;
    username: string;
    email: string;
}

export interface ResourceInfo {
    id: string;
    name: string;
    type: string;
}

export interface RuleSummary {
    id: string;
    name: string;
    description: string;
    userCount: number;
    resourceCount: number;
    createdAt: string;
    expiresAt: string | null;
    enabled: boolean;
}

export interface RuleDetail {
    id: string;
    name: string;
    description: string;
    createdAt: string;
    enabled: boolean;
    users: UserInfo[];
    resources: ResourceInfo[];
}

export const ruleApi = {
    getRules: async (): Promise<RuleSummary[]> => {
        return await getRequest('/api/v1/admin/rules');
    },

    getRuleDetail: async (id: string): Promise<RuleDetail> => {
        return await getRequest(`/api/v1/admin/rules/${id}`);
    },
    createRule: async (data: { name: string, description: string }): Promise<void> => {
        return await postRequest('/api/v1/admin/rules', data);
    },
    updateRuleUsers: async (id: string, userIds: string[]): Promise<void> => {
        return await putRequest(`/api/v1/admin/rules/${id}/users`, { userIds });
    },

    updateRuleResources: async (id: string, resourceIds: string[]): Promise<void> => {
        return await putRequest(`/api/v1/admin/rules/${id}/resources`, { resourceIds });
    },

    toggleRuleStatus: async (id: string): Promise<void> => {
        return await patchRequest(`/api/v1/admin/rules/${id}/toggle`);
    },

    deleteRule: async (id: string): Promise<void> => {
        return await deleteRequest(`/api/v1/admin/rules/${id}`);
    },

    getAllUsers: async (): Promise<UserInfo[]> => {
        return await getRequest('/api/v1/admin/users');
    },

    getAllResources: async (): Promise<ResourceInfo[]> => {
        return await getRequest('/api/v1/admin/resources');
    }
};
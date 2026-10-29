import { getRequest } from '@/shared/utils/apiClient';

export type ResourceType = 'SELF_HOSTED_WEB' | 'NETWORK_RANGE' | 'RDP' | 'SSH';

export interface Application {
    id: string;
    name: string;
    type: ResourceType;
    domain: string;
    connectorZone: string;
    identityProvider: string;
    tags: string[];
    enabled: boolean;
}

const MOCK_APPS: Application[] = [
    { id: '1', name: 'Aruba Fabric Composer', type: 'SELF_HOSTED_WEB', domain: '10.10.30.217', connectorZone: 'Homelab', identityProvider: 'Axis IdP', tags: [], enabled: true },
    { id: '2', name: 'ClearPass', type: 'SELF_HOSTED_WEB', domain: '10.10.30.201', connectorZone: 'Homelab', identityProvider: 'Axis IdP', tags: [], enabled: true },
    { id: '3', name: 'Mgmt Network', type: 'NETWORK_RANGE', domain: '10.10.30.0/24', connectorZone: 'Homelab', identityProvider: '', tags: [], enabled: true },
    { id: '4', name: 'PC-1', type: 'RDP', domain: '10.10.30.101', connectorZone: 'Homelab', identityProvider: 'Axis IdP', tags: [], enabled: true },
    { id: '5', name: 'SSH WAN Simulator', type: 'SSH', domain: '10.10.30.230', connectorZone: 'Homelab', identityProvider: '', tags: [], enabled: true },
    { id: '6', name: 'test', type: 'SELF_HOSTED_WEB', domain: '12.3.4', connectorZone: 'Homelab', identityProvider: 'Axis IdP', tags: [], enabled: true },
];

export const resourceApi = {
    getApplications: async (): Promise<Application[]> => {
        // TODO: return await getRequest('/admin/resources');
        await new Promise(resolve => setTimeout(resolve, 500));
        return MOCK_APPS;
    }
};
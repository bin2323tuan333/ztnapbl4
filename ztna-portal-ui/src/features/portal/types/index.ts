export type ResourceType = 'ssh' | 'mysql' | 'web' | 'rdp' | 'postgres' | 'redis' | 'other';

export interface Resource {
    id: string;
    name: string;
    type: ResourceType;
    description?: string;
    enabled: boolean;
}
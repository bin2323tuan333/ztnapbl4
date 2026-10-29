export type LogStatus = 'SUCCESS' | 'BLOCKED';

export interface NetworkLog {
    id: string;
    time: string;
    userName: string;
    deviceName: string;
    source: string;
    destination: string;
    status: LogStatus;
    latency: number; // ms
    port: number;
    protocol: string;
    appName: string;
    connectorZone: string;
}

export interface RouteNode {
    type: 'source' | 'pop' | 'connector' | 'destination';
    name: string;
    ip?: string;
    latencyToNext?: number; // Độ trễ tới node tiếp theo
}

export interface RouteTraceData {
    id: string;
    nodes: RouteNode[];
}

const MOCK_LOGS: NetworkLog[] = Array.from({ length: 15 }).map((_, i) => ({
    id: `log-${i}`,
    time: `11/07/2024 13:30:${(38 + i).toString().padStart(2, '0')}`,
    userName: 'Luong Pham',
    deviceName: 'Luongs-MacBook-Pro.local',
    source: `14.241.253.${36 + i}`,
    destination: '10.10.30.212',
    status: 'SUCCESS',
    latency: Math.floor(Math.random() * 100) + 20,
    port: i % 2 === 0 ? 4343 : 443,
    protocol: i % 2 === 0 ? 'TCP' : 'HTTPS',
    appName: 'Mgmt Network',
    connectorZone: 'Homelab',
}));

const MOCK_ROUTE: RouteTraceData = {
    id: 'route-1',
    nodes: [
        { type: 'source', name: 'Luongs-MacBook-Pro...', latencyToNext: 23 },
        { type: 'pop', name: 'Singapore', latencyToNext: 13 },
        { type: 'connector', name: 'Homelab', latencyToNext: 1 },
        { type: 'destination', name: '10.10.30.212', ip: '10.10.30.212' },
    ]
};

export const logApi = {
    getLogs: async (): Promise<NetworkLog[]> => {
        await new Promise(resolve => setTimeout(resolve, 500));
        return MOCK_LOGS;
    },
    getRouteTrace: async (log: NetworkLog): Promise<RouteTraceData> => {
        await new Promise(resolve => setTimeout(resolve, 300));

        return {
            id: `route-${log.id}`,
            nodes: [
                // 1. Đổi log.deviceName thành log.source (IP nguồn)
                { type: 'source', name: log.source, latencyToNext: Math.floor(Math.random() * 20) + 10 },
                { type: 'pop', name: 'Singapore', latencyToNext: Math.floor(Math.random() * 15) + 5 },
                { type: 'connector', name: log.connectorZone, latencyToNext: 1 },
                // 2. Chỉ giữ lại name, bỏ trường ip để không hiển thị 2 lần
                { type: 'destination', name: log.destination },
            ]
        };
    }
};
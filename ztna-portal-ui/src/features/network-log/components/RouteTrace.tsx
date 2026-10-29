import { Fragment } from 'react';
import { Monitor, Cloud, Network, Globe } from 'lucide-react';
import type { RouteTraceData } from '../api/logApi';

export const RouteTrace = ({ data }: { data: RouteTraceData | null }) => {
    if (!data) return null;

    const getIcon = (type: string) => {
        switch (type) {
            case 'source': return <Monitor size={20} />;
            case 'pop': return <Cloud size={20} />;
            case 'connector': return <Network size={20} />;
            case 'destination': return <Globe size={20} />;
            default: return <Globe size={20} />;
        }
    };

    return (
        <div className="route-trace-panel">
            <div className="route-trace-flow">
                {data.nodes.map((node, index) => (
                    <Fragment key={index}>
                        {/* 1. Khối Node */}
                        <div className="route-node">
                            <div className="node-icon">{getIcon(node.type)}</div>
                            <div className="node-label">{node.name}</div>
                        </div>

                        {/* 2. Khối Đường nối (Chỉ hiện nếu chưa phải node cuối) */}
                        {index < data.nodes.length - 1 && (
                            <div className="route-connector">
                                <div className="connector-line"></div>
                                <div className="connector-latency">{node.latencyToNext} ms</div>
                            </div>
                        )}
                    </Fragment>
                ))}
            </div>
        </div>
    );
};
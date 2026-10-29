import { CheckCircle2 } from 'lucide-react';
import type { NetworkLog } from '../api/logApi';

interface Props {
    logs: NetworkLog[];
    isLoading: boolean;
    selectedId: string | null;
    onSelect: (id: string) => void;
}

export const LogTable = ({ logs, isLoading, selectedId, onSelect }: Props) => {
    if (isLoading) return <div className="p-8 text-center text-gray-500">Đang tải dữ liệu...</div>;

    return (
        <div className="log-table-wrapper">
            <table className="log-table">
                <thead>
                    <tr>
                        <th>Time</th>
                        <th>User Name</th>
                        <th>Device Name</th>
                        <th>Source</th>
                        <th>Destination</th>
                        <th>Status</th>
                        <th>Latency</th>
                        <th>Port</th>
                        <th>Protocol</th>
                        <th>Application</th>
                        <th>Connector</th>
                    </tr>
                </thead>
                <tbody>
                    {logs.map((log) => (
                        <tr
                            key={log.id}
                            className={selectedId === log.id ? 'row-selected' : ''}
                            onClick={() => onSelect(log.id)}
                        >
                            <td>{log.time}</td>
                            <td>{log.userName}</td>
                            <td>{log.deviceName}</td>
                            <td>{log.source}</td>
                            <td>{log.destination}</td>
                            <td>
                                <span className="status-success">
                                    <CheckCircle2 size={14} /> Success
                                </span>
                            </td>
                            <td>{log.latency} ms</td>
                            <td>{log.port}</td>
                            <td>{log.protocol}</td>
                            <td>{log.appName}</td>
                            <td>{log.connectorZone}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <div className="table-pagination">
                <span>1 - {logs.length} of 884</span>
            </div>
        </div>
    );
};
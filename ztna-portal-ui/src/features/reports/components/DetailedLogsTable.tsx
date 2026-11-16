import type { LogRow } from '../api/reportApi';

const SHOW_ROWS = 50;   // bảng chỉ hiện 50 dòng mới nhất; "Xuất báo cáo" lấy đủ

const formatTime = (iso: string) =>
    new Date(iso).toLocaleString('vi-VN', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
    });

export const DetailedLogsTable = ({ logs }: { logs: LogRow[] }) => {
    return (
        <div className="report-panel">
            <div className="panel-header">
                <h3>Nhật ký chi tiết</h3>
                {logs.length > SHOW_ROWS && (
                    <span className="text-muted" style={{ fontSize: 13 }}>
                        Hiện {SHOW_ROWS}/{logs.length} dòng mới nhất
                    </span>
                )}
            </div>
            <div className="table-wrapper">
                <table className="simple-table">
                    <thead>
                        <tr>
                            <th>Thời gian</th>
                            <th>Người dùng</th>
                            <th>Ứng dụng</th>
                            <th>Giao thức</th>
                            <th>Trạng thái</th>
                            <th>Lý do</th>
                        </tr>
                    </thead>
                    <tbody>
                        {logs.length === 0 ? (
                            <tr><td colSpan={6} style={{ textAlign: 'center', color: '#6b7280' }}>Không có lượt truy cập nào trong khoảng thời gian này</td></tr>
                        ) : logs.slice(0, SHOW_ROWS).map((log) => (
                            <tr key={log.id}>
                                <td>{formatTime(log.time)}</td>
                                <td className="font-medium">{log.user}</td>
                                <td>{log.app}</td>
                                <td><span className="badge outline">{log.protocol}</span></td>
                                <td>
                                    <span className={`badge ${log.status === 'ALLOW' ? 'success' : 'danger'}`}>
                                        {log.status === 'ALLOW' ? 'Cho phép' : 'Bị chặn'}
                                    </span>
                                </td>
                                <td className="text-muted">{log.reason}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

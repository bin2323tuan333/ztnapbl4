export const DetailedLogsTable = ({ logs }: { logs: any[] }) => {
    return (
        <div className="report-panel">
            <div className="panel-header"><h3>Nhật ký chi tiết</h3></div>
            <div className="table-wrapper">
                <table className="simple-table">
                    <thead>
                        <tr>
                            <th>Thời gian</th>
                            <th>Người dùng</th>
                            <th>Ứng dụng</th>
                            <th>Giao thức</th>
                            <th>Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        {logs.map((log) => (
                            <tr key={log.id}>
                                <td>{log.time}</td>
                                <td className="font-medium">{log.user}</td>
                                <td>{log.app}</td>
                                <td><span className="badge outline">{log.protocol}</span></td>
                                <td>
                                    <span className={`badge ${log.status === 'SUCCESS' ? 'success' : 'danger'}`}>
                                        {log.status === 'SUCCESS' ? 'Thành công' : 'Bị chặn'}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
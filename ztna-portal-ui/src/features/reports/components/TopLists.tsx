export const TopLists = ({ topUsers, topApps }: { topUsers: any[], topApps: any[] }) => {
    return (
        <div className="report-top-lists">
            <div className="report-panel">
                <div className="panel-header"><h3>Top Người dùng</h3></div>
                <div className="top-list">
                    {topUsers.map((u, i) => (
                        <div key={u.id} className="top-item">
                            <span className="top-rank">{i + 1}</span>
                            <span className="top-name">{u.name}</span>
                            <span className="top-count">{u.count} reqs</span>
                        </div>
                    ))}
                </div>
            </div>
            <div className="report-panel">
                <div className="panel-header"><h3>Top Ứng dụng</h3></div>
                <div className="top-list">
                    {topApps.map((a, i) => (
                        <div key={a.id} className="top-item">
                            <span className="top-rank">{i + 1}</span>
                            <span className="top-name">{a.name}</span>
                            <span className="top-count">{a.count} reqs</span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};
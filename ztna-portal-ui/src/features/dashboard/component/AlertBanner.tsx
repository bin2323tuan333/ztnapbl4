import { AlertTriangle } from 'lucide-react';

export const AlertBanner = () => {
    return (
        <div className="dashboard-alert-banner">
            <AlertTriangle size={20} color="#b45309" />
            <span>Có <strong>2</strong> Connector đang Offline và <strong>5</strong> quyền sắp hết hạn.</span>
            <button>Xem ngay</button>
        </div>
    );
};
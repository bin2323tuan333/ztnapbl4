import { useNavigate } from 'react-router-dom';
import { Shield, Lock, Eye, ArrowRight } from 'lucide-react';
import './Overview.css';
import { getCurrentUser } from '../utils/auth';

const Overview = () => {
    const navigate = useNavigate();

    const features = [
        {
            icon: <Shield size={28} />,
            title: 'Zero Trust Security',
            description: 'Xác thực mọi yêu cầu truy cập, không tin tưởng mặc định bất kỳ kết nối nào.',
        },
        {
            icon: <Lock size={28} />,
            title: 'Kiểm soát truy cập',
            description: 'Quản lý quyền truy cập chi tiết theo người dùng, tài nguyên và thời gian.',
        },
        {
            icon: <Eye size={28} />,
            title: 'Giám sát toàn diện',
            description: 'Theo dõi và ghi nhật ký mọi hoạt động truy cập trong thời gian thực.',
        },
    ];

    return (
        <div className="ov-page">
            <div className="ov-hero">
                <div className="ov-hero-content">
                    <div className="ov-logo">
                        <Shield size={40} />
                    </div>
                    <h1 className="ov-title">ZTNA System</h1>
                    <p className="ov-tagline">Zero Trust Network Access</p>
                    <p className="ov-description">
                        Hệ thống kiểm soát truy cập mạng theo mô hình Zero Trust.
                        Bảo vệ tài nguyên doanh nghiệp với xác thực đa lớp và kiểm soát chi tiết.
                    </p>
                    <div className="ov-actions">
                        <button className="ov-btn-primary" onClick={() => {
                            const me = getCurrentUser();
                            if (me == null) {
                                navigate("/login");
                                return;
                            }

                            if (me?.role === 'ADMIN') navigate("/app/home");
                            else if (me?.role === 'USER') navigate("/portal");
                        }}>
                            <span>Đăng nhập</span>
                            <ArrowRight size={18} />
                        </button>
                        <button className="ov-btn-ghost">
                            Tìm hiểu thêm
                        </button>
                    </div>
                </div>
            </div>

            <div className="ov-features">
                <div className="ov-features-grid">
                    {features.map((feature, index) => (
                        <div key={index} className="ov-feature-card">
                            <div className="ov-feature-icon">{feature.icon}</div>
                            <h3 className="ov-feature-title">{feature.title}</h3>
                            <p className="ov-feature-desc">{feature.description}</p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Overview;
import { Shield, ArrowRight } from 'lucide-react';
import { useLoginNavigation } from '../hooks/useLoginNavigation';

const HeroSection = () => {
    const { handleLoginClick } = useLoginNavigation();

    return (
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
                    <button className="ov-btn-primary" onClick={handleLoginClick}>
                        <span>Đăng nhập</span>
                        <ArrowRight size={18} />
                    </button>
                    <button className="ov-btn-ghost">
                        Tìm hiểu thêm
                    </button>
                </div>
            </div>
        </div>
    );
}

export default HeroSection;
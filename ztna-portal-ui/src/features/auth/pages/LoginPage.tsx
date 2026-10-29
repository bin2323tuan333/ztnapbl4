import { Shield } from 'lucide-react';
import { LoginForm } from '../component/LoginForm';
import '../styles/LoginPage.css';


const LoginPage = () => {
    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-logo">
                    <Shield size={32} />
                </div>
                <h1 className="login-title">ZTNA System</h1>
                <p className="login-subtitle">Đăng nhập để tiếp tục</p>

                <LoginForm />
            </div>
        </div>
    );
};

export default LoginPage;
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, User, Lock, LogIn } from 'lucide-react';
import './Login.css';

const Login: React.FC = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        setTimeout(() => {
            if (username === 'admin' && password === '123') {
                localStorage.setItem('token', 'admin-token-xyz');
                localStorage.setItem('username', 'admin');
                localStorage.setItem('role', 'ADMIN');
                localStorage.setItem('allowedApps', JSON.stringify(['*']));
                navigate('/app/home');
            } else if (username === 'user' && password === '123') {
                localStorage.setItem('token', 'user-token-xyz');
                localStorage.setItem('username', 'user');
                localStorage.setItem('role', 'USER');
                localStorage.setItem('allowedApps', JSON.stringify(['portal']));
                navigate('/portal');
            } else {
                setError('Tên đăng nhập hoặc mật khẩu không đúng');
            }
            setLoading(false);
        }, 500);
    };

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-logo">
                    <Shield size={32} />
                </div>
                <h1 className="login-title">ZTNA System</h1>
                <p className="login-subtitle">Đăng nhập để tiếp tục</p>

                <form onSubmit={handleSubmit} className="login-form">
                    {error && <div className="login-error">{error}</div>}

                    <div className="login-field">
                        <div className="login-input-wrapper">
                            <User size={18} className="login-input-icon" />
                            <input
                                type="text"
                                placeholder="Tên đăng nhập"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="login-input"
                                required
                            />
                        </div>
                    </div>

                    <div className="login-field">
                        <div className="login-input-wrapper">
                            <Lock size={18} className="login-input-icon" />
                            <input
                                type="password"
                                placeholder="Mật khẩu"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="login-input"
                                required
                            />
                        </div>
                    </div>

                    <button type="submit" className="login-btn" disabled={loading}>
                        <LogIn size={18} />
                        <span>{loading ? 'Đang đăng nhập...' : 'Đăng nhập'}</span>
                    </button>
                </form>

                <p className="login-hint">
                    Demo: <strong>admin</strong> / <strong>123</strong> hoặc <strong>user</strong> / <strong>123</strong>
                </p>
            </div>
        </div>
    );
};

export default Login;
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, User, Lock, LogIn } from 'lucide-react';
import { getCurrentUser } from '../utils/auth';
import './Login.css';
import { postRequest } from '../utils/apiClient';

const Login = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (!username || !password) {
            setError("Kiểm tra lại tên đăng nhập và mật khẩu");
            return;
        }

        setLoading(true);
        try {
            const data = await postRequest("/auth/login", {
                username,
                password
            })

            localStorage.setItem('accessToken', data.accessToken);

            const me = getCurrentUser();
            if (me?.role === 'ADMIN') navigate("/app/home");
            else if (me?.role === 'USER') navigate("/portal");

        } catch (err: any) {
            console.error(err.message);
            setError("Không kết nối được server");
        } finally {
            setLoading(false);
        }
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
                            />
                        </div>
                    </div>

                    <button type="submit" className="login-btn" disabled={loading}>
                        <LogIn size={18} />
                        <span>{loading ? 'Đang đăng nhập...' : 'Đăng nhập'}</span>
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;
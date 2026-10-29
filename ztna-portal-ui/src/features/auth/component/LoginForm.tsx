// src/features/auth/components/LoginForm.tsx
import { User, Lock, LogIn } from 'lucide-react';
import { useLogin } from '../hooks/useLogin';

export const LoginForm = () => {
    const {
        username, setUsername,
        password, setPassword,
        error, loading,
        handleSubmit
    } = useLogin();

    return (
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
    );
};
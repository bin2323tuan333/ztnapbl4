import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { getCurrentUser } from '@/shared/utils/auth';

export const useLogin = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (!username.trim() || !password.trim()) {
            setError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
            return;
        }

        setLoading(true);
        try {
            const data = await authApi.login({ username, password });

            localStorage.removeItem('accessToken');
            localStorage.setItem('accessToken', data.accessToken);

            const me = getCurrentUser();

            if (me?.role === 'ADMIN') {
                navigate("/dashboard");
            } else if (me?.role === 'USER') {
                navigate("/portal");
                // } else if (me?.role === 'SUPERVISOR') {
                //     // Nếu bạn có trang riêng cho giám sát
                //     navigate("/dashboard"); 
            } else {
                navigate("/portal");
            }

        } catch (err: any) {
            console.error("Login Error:", err);
            if (err.status === 401) {
                setError("Sai tên đăng nhập hoặc mật khẩu");
            } else if (err.status === 403) {
                setError("Tài khoản của bạn đã bị khóa");
            } else if (err.status === 400) {
                setError(err.message || "Dữ liệu không hợp lệ");
            } else {
                setError("Không kết nối được đến máy chủ");
            }
        } finally {
            setLoading(false);
        }
    };

    return {
        username, setUsername,
        password, setPassword,
        error, loading,
        handleSubmit
    };
};
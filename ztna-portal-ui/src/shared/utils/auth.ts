import { jwtDecode } from 'jwt-decode';

export interface JwtPayload {
    sub: string;
    role: string
    exp: number;
}

export function getCurrentUser(): JwtPayload | null {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    try {
        const payload = jwtDecode<JwtPayload>(token);
        return payload;
    } catch {
        return null;
    }
}

/**
 * Đăng xuất: gọi server thu hồi refresh token (cookie httpOnly) rồi xoá dữ liệu phía trình duyệt.
 * Server lỗi hoặc mất mạng vẫn xoá dữ liệu local để người dùng thoát được.
 */
export async function logout(): Promise<void> {
    try {
        await fetch(`http://${window.location.hostname}:443/api/v1/auth/logout`, {
            method: 'POST',
            credentials: 'include',
        });
    } catch {
        // bỏ qua: vẫn xoá phía trình duyệt
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
}

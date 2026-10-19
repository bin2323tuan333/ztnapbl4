import { jwtDecode } from 'jwt-decode';

export interface JwtPayload {
    sub: string;
    userId: number;
    role: 'ADMIN' | 'USER';
    allowedApps: string[];
    exp: number;
}

export function getCurrentUser(): JwtPayload | null {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
        const payload = jwtDecode<JwtPayload>(token);
        if (payload.exp * 1000 < Date.now()) {
            localStorage.removeItem('token');
            return null;
        }
        return payload;
    } catch {
        return null;
    }
}

export function logout() {
    localStorage.removeItem('token');
}
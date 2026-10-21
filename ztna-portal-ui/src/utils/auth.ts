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

export function logout() {
    localStorage.removeItem('token');
}
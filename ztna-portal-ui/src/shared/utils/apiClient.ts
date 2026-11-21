const BACKEND_URL = 'http://localhost:443';

// Nhiều API gọi cùng lúc (vd Dashboard) đều có thể nhận 401 khi access token hết hạn.
// Dùng chung 1 lần gọi /refresh cho tất cả, tránh refresh nhiều lần và ghi log lặp.
let refreshPromise: Promise<string> | null = null;

const refreshAccessToken = (): Promise<string> => {
    if (!refreshPromise) {
        refreshPromise = fetch(`${BACKEND_URL}/api/v1/auth/refresh`, {
            method: 'POST',
            credentials: 'include',
        })
            .then(async (res) => {
                if (!res.ok) throw new Error('Refresh token hết hạn');
                const data = await res.json();
                localStorage.setItem('accessToken', data.accessToken);
                return data.accessToken as string;
            })
            .finally(() => {
                refreshPromise = null;   // lần hết hạn sau sẽ refresh lại
            });
    }
    return refreshPromise;
};

const request = async (path: string, method: string, body?: any, retried = false): Promise<any> => {
    const token = localStorage.getItem('accessToken');

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options: RequestInit = {
        method,
        headers,
        credentials: 'include',
    };

    if (body && method !== 'GET') {
        options.body = JSON.stringify(body);
    }

    const res = await fetch(`${BACKEND_URL}${path}`, options);

    // retried = true: đã refresh rồi mà vẫn 401 thì không refresh nữa (tránh lặp vô hạn)
    if (res.status === 401 && !retried && path !== '/api/v1/auth/login' && path !== '/api/v1/auth/refresh') {
        try {
            await refreshAccessToken();
        } catch (e) {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }
        return request(path, method, body, true);   // gọi lại 1 lần với access token mới
    }

    const text = await res.text();
    let data: any = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch (e) {
            data = { message: text };
        }
    }

    if (!res.ok) {
        const errorMessage = data?.message || data?.error || `Lỗi HTTP: ${res.status}`;
        const error: any = new Error(errorMessage);
        error.status = res.status;
        error.data = data;
        throw error;
    }

    return data;
};

export const getRequest = (path: string) => request(path, 'GET');
export const patchRequest = (path: string, body?: any) => request(path, 'PATCH', body);
export const postRequest = (path: string, body: any) => request(path, 'POST', body);
export const putRequest = (path: string, body: any) => request(path, 'PUT', body);
export const deleteRequest = (path: string) => request(path, 'DELETE');

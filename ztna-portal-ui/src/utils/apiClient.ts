const BACKEND_URL = 'http://localhost:443';

const request = async (path: string, method: string, body?: any) => {
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

    if (res.status === 401 && path !== '/auth/login' && path !== '/auth/refresh') {
        try {
            const refreshRes = await fetch(`${BACKEND_URL}/auth/refresh`, {
                method: 'POST',
                credentials: 'include'
            });

            if (refreshRes.ok) {
                const refreshData = await refreshRes.json();
                localStorage.setItem('accessToken', refreshData.accessToken);
                return request(path, method, body);
            } else {
                localStorage.removeItem('accessToken');
                window.location.href = '/login';
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            }
        } catch (e) {

        }
    }
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;

    if (!res.ok) {
        const error: any = new Error(data?.message || 'Đã có lỗi xảy ra');
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

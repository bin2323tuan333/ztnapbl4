const BACKEND_URL = 'http://localhost:443';

const request = async (path: string, method: string, body?: any): Promise<any> => {
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

    // 1. Xử lý tự động Refresh Token khi bị 401
    if (res.status === 401 && path !== '/api/v1/auth/login' && path !== '/api/v1/auth/refresh') {
        try {
            const refreshRes = await fetch(`${BACKEND_URL}/api/v1/auth/refresh`, {
                method: 'POST',
                credentials: 'include'
            });

            if (refreshRes.ok) {
                const refreshData = await refreshRes.json();
                localStorage.setItem('accessToken', refreshData.accessToken);
                // Gọi lại request ban đầu với token mới
                return request(path, method, body);
            } else {
                throw new Error('Refresh token hết hạn');
            }
        } catch (e) {
            // Nếu refresh thất bại, xóa token và đá về trang login
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }
    }

    // 2. Đọc dữ liệu trả về một cách AN TOÀN (không bị crash nếu là HTML)
    const text = await res.text();
    let data: any = null;

    if (text) {
        try {
            data = JSON.parse(text); // Thử parse JSON
        } catch (e) {
            data = { message: text }; // Nếu là HTML/Text thì đưa vào message
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
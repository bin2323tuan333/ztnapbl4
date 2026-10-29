import { postRequest } from '@/shared/utils/apiClient';

export interface LoginCredentials {
    username: string;
    password: string;
}

export const authApi = {
    login: (credentials: LoginCredentials) => {
        return postRequest('/api/v1/auth/login', credentials);
    }
};
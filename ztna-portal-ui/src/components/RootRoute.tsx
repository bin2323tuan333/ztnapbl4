import { Navigate } from 'react-router-dom';
import type { SystemRoleCode } from '../types';

const RootRoute = () => {
    const token = localStorage.getItem('token');
    const roleCode = localStorage.getItem('role') as SystemRoleCode | null;

    if (!token || !roleCode) {
        return <Navigate to="/" replace />;
    }

    switch (roleCode) {
        case 'ADMIN':
        case 'SUPERVISOR':
            return <Navigate to="/app/home" replace />;

        case 'TARGET_USER':
            return <Navigate to="/portal" replace />;

        default:
            return <Navigate to="/" replace />;
    }
};

export default RootRoute;
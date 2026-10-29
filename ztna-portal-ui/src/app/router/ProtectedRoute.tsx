import { Navigate, Outlet } from 'react-router-dom';
import { getCurrentUser } from '../../shared/utils/auth';

interface ProtectedRouteProps {
    allowedRoles?: string[];
}

const ProtectedRoute = ({ allowedRoles }: ProtectedRouteProps) => {
    const me = getCurrentUser();

    if (me == null) return <Navigate to="/login" replace />;

    if (allowedRoles && !allowedRoles.includes(me?.role)) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;
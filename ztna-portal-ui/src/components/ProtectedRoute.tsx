import { Navigate, Outlet } from 'react-router-dom';
import { getCurrentUser } from '../utils/auth';

interface ProtectedRouteProps {
    allowedRoles?: string[];
}

const ProtectedRoute = ({ allowedRoles }: ProtectedRouteProps) => {
    const me = getCurrentUser();

    console.log("Role hiện tại của user là:", me?.role);
    console.log("Danh sách quyền được phép vào:", allowedRoles);


    if (me == null) return <Navigate to="/login" replace />;

    if (allowedRoles && !allowedRoles.includes(me?.role)) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;
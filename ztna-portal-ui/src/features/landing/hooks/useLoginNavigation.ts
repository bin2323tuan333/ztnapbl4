// src/features/landing/hooks/useLoginNavigation.js
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '@/shared/utils/auth.ts'; // Đảm bảo đường dẫn này đúng với dự án của bạn

export function useLoginNavigation() {
    const navigate = useNavigate();

    const handleLoginClick = () => {
        const me = getCurrentUser();

        if (me == null) {
            navigate("/login");
            return;
        }

        if (me?.role === 'ADMIN') {
            navigate("/dashboard");
        } else {
            navigate("/portal"); // USER và SUPERVISOR
        }
    };

    return { handleLoginClick };
}

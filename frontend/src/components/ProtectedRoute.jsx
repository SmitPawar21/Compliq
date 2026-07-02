import { Navigate, Outlet } from 'react-router-dom';
import { getCookie } from '../utils/cookies';

const ProtectedRoute = () => {
    const token = getCookie('token');

    if (!token) {
        // Redirect to login page if token is missing
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;

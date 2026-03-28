import { Navigate, useLocation } from 'react-router-dom';
import { getStoredAccessToken } from '../../lib/authStorage';

interface AuthGuardProps {
    children: React.ReactNode;
}
const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
    const token = getStoredAccessToken();
    const location = useLocation();

    if (!token) {
        // Redirect to login page with state to redirect back after login
        // Or if you have a modal, you might trigger it here, but for route protection, redirect is safer.
        // If you want to use the modal, you would need a global state or context to open it.
        // For now, let's redirect to home with a query param or just redirect to home.
        // Since we don't have a dedicated /login page (it's a modal), we might redirect to home.
        // But ideally, we should show the login modal.

        // Assuming we might have a dedicated login page later or handled via home
        return <Navigate to="/" state={{ from: location, openLogin: true }} replace />;
    }

    return <>{children}</>;
};

export default AuthGuard;

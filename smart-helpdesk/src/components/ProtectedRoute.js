import { Navigate } from 'react-router-dom';

const AUTH_DISABLED = process.env.REACT_APP_DISABLE_AUTH === 'true';

function ProtectedRoute({ children, allowedRoles, requiredPermission }) {
  if (AUTH_DISABLED) {
    return children;
  }

  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const permissions = JSON.parse(localStorage.getItem('permissions') || '{}');
  
  if (!user) {
    return <Navigate to="/login" />;
  }

  if (allowedRoles?.length && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  if (requiredPermission && permissions[requiredPermission] === false) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default ProtectedRoute;

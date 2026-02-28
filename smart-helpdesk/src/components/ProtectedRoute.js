import { Navigate } from 'react-router-dom';

const AUTH_DISABLED = process.env.REACT_APP_DISABLE_AUTH === 'true';

function ProtectedRoute({ children }) {
  if (AUTH_DISABLED) {
    return children;
  }

  const user = JSON.parse(localStorage.getItem('user'));
  
  if (!user) {
    return <Navigate to="/login" />;
  }

  return children;
}

export default ProtectedRoute;

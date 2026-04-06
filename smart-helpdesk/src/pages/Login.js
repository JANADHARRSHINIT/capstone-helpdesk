import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchPermissions, login } from '../services/api';
import './Login.css';

const AUTH_DISABLED = process.env.REACT_APP_DISABLE_AUTH === 'true';
const QUICK_ACCOUNTS = [
  { label: 'Admin', email: 'admin@helpdesk.com', password: 'admin123', className: 'admin', fallbackRole: 'ADMIN' },
  { label: 'John', email: 'john@helpdesk.com', password: 'employee123', className: 'employee', fallbackRole: 'EMPLOYEE' },
  { label: 'Jane', email: 'jane@helpdesk.com', password: 'employee123', className: 'employee', fallbackRole: 'EMPLOYEE' },
  { label: 'Mike', email: 'mike@helpdesk.com', password: 'employee123', className: 'employee', fallbackRole: 'EMPLOYEE' },
  { label: 'User', email: 'alice@company.com', password: 'user123', className: 'customer', fallbackRole: 'USER' }
];

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (AUTH_DISABLED) {
        const fallbackUser = {
          id: Date.now(),
          name: email?.split('@')[0] || 'Local User',
          email: email || 'local@helpdesk.dev',
          role: 'ADMIN'
        };
        localStorage.setItem('user', JSON.stringify(fallbackUser));
        localStorage.setItem('permissions', JSON.stringify({}));
        navigate('/dashboard');
        return;
      }

      const user = await login(email, password);
      localStorage.setItem('user', JSON.stringify(user));
      try {
        const permissions = await fetchPermissions(user.role);
        localStorage.setItem('permissions', JSON.stringify(permissions.modules));
      } catch (_) {
        localStorage.setItem('permissions', JSON.stringify({}));
      }
      navigate('/dashboard');
    } catch (error) {
      alert(error.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const quickLogin = async (account) => {
    try {
      setLoading(true);
      if (AUTH_DISABLED) {
        const fallbackUser = {
          id: Date.now(),
          name: `${account.label} Local`,
          email: account.email,
          role: account.fallbackRole
        };
        localStorage.setItem('user', JSON.stringify(fallbackUser));
        localStorage.setItem('permissions', JSON.stringify({}));
        navigate('/dashboard');
        return;
      }

      const user = await login(account.email, account.password);
      localStorage.setItem('user', JSON.stringify(user));
      try {
        const permissions = await fetchPermissions(user.role);
        localStorage.setItem('permissions', JSON.stringify(permissions.modules));
      } catch (_) {
        localStorage.setItem('permissions', JSON.stringify({}));
      }
      navigate('/dashboard');
    } catch (error) {
      alert(error.message || 'Quick login failed. Start the backend server and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-left">
        <div className="login-left-content">
          <h1 className="login-brand">&#128736;&#65039; IT HelpDesk</h1>
          <p className="login-tagline">Your Complete IT Support Solution</p>
          <ul className="login-features">
            <li>&#10003; 24/7 Ticket Management</li>
            <li>&#10003; Real-time Status Updates</li>
            <li>&#10003; AI-Powered Chatbot</li>
            <li>&#10003; Priority-based Resolution</li>
          </ul>
        </div>
      </div>

      <div className="login-right">
        <div className="login-card">
          <h2 className="login-title">Welcome Back</h2>
          <p className="login-subtitle">Sign in to your account</p>

          <form onSubmit={handleLogin} className="login-form">
            <div className="form-group">
              <label>Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                required
              />
            </div>

            <div className="form-group">
              <label>Password</label>
              <div className="password-field">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((prev) => !prev)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <div className="form-options">
              <label className="checkbox-label">
                <input type="checkbox" />
                <span>Remember me</span>
              </label>
              <button type="button" className="forgot-link">Forgot password?</button>
            </div>

            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>

          <div className="login-divider">
            <span>Quick Login (Demo)</span>
          </div>

          <div className="quick-login-btns">
            {QUICK_ACCOUNTS.map((account) => (
              <button
                key={account.email}
                onClick={() => quickLogin(account)}
                className={`quick-btn ${account.className}`}
                disabled={loading}
              >
                {account.label}
              </button>
            ))}
          </div>

          <p className="signup-link">
            Don&apos;t have an account? <a href="/signup">Sign up</a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;

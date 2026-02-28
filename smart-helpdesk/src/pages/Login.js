import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { users } from '../data/mockData';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    const user = users.find(u => u.email === email);
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
      navigate('/dashboard');
    } else {
      alert('Invalid credentials');
    }
  };

  const quickLogin = (role) => {
    const user = users.find(u => u.role === role);
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
      navigate('/dashboard');
    }
  };

  return (
    <div className="login-container">
      <div className="login-left">
        <div className="login-left-content">
          <h1 className="login-brand">🛠️ IT HelpDesk</h1>
          <p className="login-tagline">Your Complete IT Support Solution</p>
          <ul className="login-features">
            <li>✓ 24/7 Ticket Management</li>
            <li>✓ Real-time Status Updates</li>
            <li>✓ AI-Powered Chatbot</li>
            <li>✓ Priority-based Resolution</li>
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
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
              />
            </div>

            <div className="form-options">
              <label className="checkbox-label">
                <input type="checkbox" />
                <span>Remember me</span>
              </label>
              <a href="#" className="forgot-link">Forgot password?</a>
            </div>

            <button type="submit" className="login-btn">Sign In</button>
          </form>

          <div className="login-divider">
            <span>Quick Login (Demo)</span>
          </div>

          <div className="quick-login-btns">
            <button onClick={() => quickLogin('ADMIN')} className="quick-btn admin">Admin</button>
            <button onClick={() => quickLogin('EMPLOYEE')} className="quick-btn employee">Employee</button>
            <button onClick={() => quickLogin('CUSTOMER')} className="quick-btn customer">Customer</button>
          </div>

          <p className="signup-link">
            Don't have an account? <a href="/signup">Sign up</a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;

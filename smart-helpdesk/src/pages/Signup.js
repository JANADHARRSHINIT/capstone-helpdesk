import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../services/api';
import './Signup.css';

const ROLE_OPTIONS = [
  { value: 'USER', label: 'User (Customer)' },
  { value: 'EMPLOYEE', label: 'Employee (Support Staff)' },
  { value: 'ADMIN', label: 'Admin' }
];

function Signup() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    role: '',
    employeeId: '',
    adminCode: ''
  });
  const [errors, setErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState({ type: '', text: '' });
  const navigate = useNavigate();

  const roleHint = useMemo(() => {
    if (formData.role === 'EMPLOYEE') return 'Support staff members need a valid employee ID.';
    if (formData.role === 'ADMIN') return 'Admin signup requires the secure admin code.';
    if (formData.role === 'USER') return 'Customers can raise and track support tickets after signup.';
    return 'Choose the account type that matches your access level.';
  }, [formData.role]);

  const validate = () => {
    const newErrors = {};

    if (!formData.name.trim()) newErrors.name = 'Full name is required';

    if (!formData.email.trim()) {
      newErrors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Enter a valid email address';
    }

    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = 'Phone number is required';
    } else if (!/^[0-9+()\-\s]{7,20}$/.test(formData.phoneNumber.trim())) {
      newErrors.phoneNumber = 'Enter a valid phone number';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (!formData.role) newErrors.role = 'Please select a role';

    if (formData.role === 'EMPLOYEE' && !formData.employeeId.trim()) {
      newErrors.employeeId = 'Employee ID is required';
    }

    if (formData.role === 'ADMIN' && !formData.adminCode.trim()) {
      newErrors.adminCode = 'Admin code is required';
    }

    return newErrors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'role' && value !== 'EMPLOYEE' ? { employeeId: '' } : {}),
      ...(name === 'role' && value !== 'ADMIN' ? { adminCode: '' } : {})
    }));

    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
    if (name === 'role') {
      setErrors((prev) => ({ ...prev, employeeId: '', adminCode: '', role: '' }));
    }
    if (submitMessage.text) {
      setSubmitMessage({ type: '', text: '' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      setSubmitMessage({ type: 'error', text: 'Please fix the highlighted fields and try again.' });
      return;
    }

    try {
      setSubmitting(true);
      setSubmitMessage({ type: '', text: '' });

      await signup({
        name: formData.name.trim(),
        email: formData.email.trim(),
        phoneNumber: formData.phoneNumber.trim(),
        password: formData.password,
        role: formData.role,
        employeeId: formData.role === 'EMPLOYEE' ? formData.employeeId.trim() : null,
        adminCode: formData.role === 'ADMIN' ? formData.adminCode.trim() : null
      });

      setSubmitMessage({ type: 'success', text: 'Account created successfully. Redirecting to login...' });
      setTimeout(() => navigate('/login'), 1200);
    } catch (error) {
      setSubmitMessage({ type: 'error', text: error.message || 'Signup failed. Please try again.' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-left">
        <div className="signup-left-content">
          <h1 className="signup-brand">&#128736;&#65039; Smart IT Helpdesk</h1>
          <p className="signup-tagline">Create your workspace access in minutes</p>
          <ul className="signup-features">
            <li>&#10003; Smart ticket routing</li>
            <li>&#10003; Role-based access for users, employees, and admins</li>
            <li>&#10003; Real-time support updates</li>
            <li>&#10003; Secure account onboarding</li>
          </ul>
        </div>
      </div>

      <div className="signup-right">
        <div className="signup-card">
          <h2 className="signup-title">Create Account</h2>
          <p className="signup-subtitle">Sign up to get started with Smart IT Helpdesk</p>

          <form onSubmit={handleSubmit} className="signup-form">
            <div className="form-group">
              <label>Role Selection</label>
              <select name="role" value={formData.role} onChange={handleChange} className={errors.role ? 'error' : ''}>
                <option value="">Select your role</option>
                {ROLE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
              <p className="field-hint">{roleHint}</p>
              {errors.role && <span className="error-text">{errors.role}</span>}
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" name="name" value={formData.name} onChange={handleChange} className={errors.name ? 'error' : ''} placeholder="Enter your full name" />
                {errors.name && <span className="error-text">{errors.name}</span>}
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} className={errors.phoneNumber ? 'error' : ''} placeholder="Enter your phone number" />
                {errors.phoneNumber && <span className="error-text">{errors.phoneNumber}</span>}
              </div>
            </div>

            <div className="form-group">
              <label>Email Address</label>
              <input type="email" name="email" value={formData.email} onChange={handleChange} className={errors.email ? 'error' : ''} placeholder="Enter your email address" />
              {errors.email && <span className="error-text">{errors.email}</span>}
            </div>

            {formData.role === 'EMPLOYEE' && (
              <div className="form-group role-field">
                <label>Employee ID</label>
                <input type="text" name="employeeId" value={formData.employeeId} onChange={handleChange} className={errors.employeeId ? 'error' : ''} placeholder="Enter your employee ID" />
                {errors.employeeId && <span className="error-text">{errors.employeeId}</span>}
              </div>
            )}

            {formData.role === 'ADMIN' && (
              <div className="form-group role-field">
                <label>Admin Code</label>
                <input type="text" name="adminCode" value={formData.adminCode} onChange={handleChange} className={errors.adminCode ? 'error' : ''} placeholder="Enter the secure admin code" />
                {errors.adminCode && <span className="error-text">{errors.adminCode}</span>}
              </div>
            )}

            <div className="form-grid">
              <div className="form-group">
                <label>Password</label>
                <div className="password-field">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    className={errors.password ? 'error' : ''}
                    placeholder="Create a password"
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword((prev) => !prev)}
                  >
                    {showPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
                <p className="field-hint">Use at least 8 characters for stronger security.</p>
                {errors.password && <span className="error-text">{errors.password}</span>}
              </div>

              <div className="form-group">
                <label>Confirm Password</label>
                <div className="password-field">
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={errors.confirmPassword ? 'error' : ''}
                    placeholder="Confirm your password"
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowConfirmPassword((prev) => !prev)}
                  >
                    {showConfirmPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
                {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
              </div>
            </div>

            {submitMessage.text && (
              <div className={`submit-message ${submitMessage.type}`}>
                {submitMessage.text}
              </div>
            )}

            <button type="submit" className="signup-btn" disabled={submitting}>
              {submitting ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>

          <p className="login-link">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Signup;

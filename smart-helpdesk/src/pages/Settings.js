import { useState } from 'react';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import './Settings.css';

function Settings() {
  const user = JSON.parse(localStorage.getItem('user'));
  const [formData, setFormData] = useState({
    name: user.name,
    email: user.email,
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleProfileUpdate = (e) => {
    e.preventDefault();
    alert('Profile updated successfully!');
  };

  const handlePasswordChange = (e) => {
    e.preventDefault();
    if (formData.newPassword !== formData.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }
    alert('Password changed successfully!');
    setFormData({ ...formData, currentPassword: '', newPassword: '', confirmPassword: '' });
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      
      <div className="dashboard-main">
        <Navbar title="Settings" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="settings-container">
            <div className="settings-card">
              <h3>Profile Information</h3>
              <form onSubmit={handleProfileUpdate}>
                <div className="form-group">
                  <label>Full Name</label>
                  <input type="text" name="name" value={formData.name} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" name="email" value={formData.email} onChange={handleChange} disabled />
                </div>
                <div className="form-group">
                  <label>Role</label>
                  <input type="text" value={user.role} disabled />
                </div>
                <button type="submit" className="btn-primary">Update Profile</button>
              </form>
            </div>

            <div className="settings-card">
              <h3>Change Password</h3>
              <form onSubmit={handlePasswordChange}>
                <div className="form-group">
                  <label>Current Password</label>
                  <input type="password" name="currentPassword" value={formData.currentPassword} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>New Password</label>
                  <input type="password" name="newPassword" value={formData.newPassword} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Confirm New Password</label>
                  <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required />
                </div>
                <button type="submit" className="btn-primary">Change Password</button>
              </form>
            </div>

            <div className="settings-card">
              <h3>Preferences</h3>
              <div className="form-group">
                <label>
                  <input type="checkbox" defaultChecked />
                  <span>Email notifications for ticket updates</span>
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input type="checkbox" defaultChecked />
                  <span>SMS notifications for urgent tickets</span>
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input type="checkbox" />
                  <span>Weekly summary reports</span>
                </label>
              </div>
              <button className="btn-primary">Save Preferences</button>
            </div>
          </div>
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Settings;

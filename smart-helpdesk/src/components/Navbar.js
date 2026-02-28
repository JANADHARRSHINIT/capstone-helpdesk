import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ title, userName }) {
  const [showDropdown, setShowDropdown] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <div className="navbar">
      <h1 className="navbar-title">{title}</h1>

      <div className="navbar-right">
        <div className="navbar-search">
          <input type="text" placeholder="Search..." />
          <span className="search-icon" aria-hidden="true">&#128269;</span>
        </div>

        <button className="navbar-notification" aria-label="Notifications">
          <span aria-hidden="true">&#128276;</span>
          <span className="notification-badge">3</span>
        </button>

        <div className="navbar-profile">
          <button className="profile-btn" onClick={() => setShowDropdown(!showDropdown)}>
            <div className="profile-avatar">{userName?.charAt(0)}</div>
            <span className="profile-name">{userName}</span>
          </button>

          {showDropdown && (
            <div className="profile-dropdown">
              <button onClick={handleLogout} className="dropdown-item">
                <span aria-hidden="true">&#128682;</span>
                <span>Logout</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Navbar;

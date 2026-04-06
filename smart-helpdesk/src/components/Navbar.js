import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ title, userName }) {
  const [showDropdown, setShowDropdown] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const fetchNotifications = useCallback(async () => {
    if (!user.id) return;
    try {
      const [notifResponse, countResponse] = await Promise.all([
        fetch(`http://localhost:8080/api/notifications/${user.id}`),
        fetch(`http://localhost:8080/api/notifications/${user.id}/unread-count`)
      ]);
      const notifData = await notifResponse.json();
      const countData = await countResponse.json();
      setNotifications(notifData.slice(0, 5));
      setUnreadCount(countData.count);
    } catch (error) {
      console.error('Failed to fetch notifications');
    }
  }, [user.id]);

  useEffect(() => {
    if (!user.id) return undefined;
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [user.id, fetchNotifications]);

  const markAsRead = async (id) => {
    try {
      await fetch(`http://localhost:8080/api/notifications/${id}/read`, { method: 'PUT' });
      fetchNotifications();
    } catch (error) {
      console.error('Failed to mark as read');
    }
  };

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

        <button 
          className="navbar-notification" 
          aria-label="Notifications"
          onClick={() => setShowNotifications(!showNotifications)}
        >
          <span aria-hidden="true">&#128276;</span>
          {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
        </button>

        {showNotifications && (
          <div className="notifications-dropdown">
            <h4>Notifications</h4>
            {notifications.length === 0 ? (
              <p className="no-notifications">No notifications</p>
            ) : (
              notifications.map(notif => (
                <div 
                  key={notif.id} 
                  className={`notification-item ${notif.isRead ? 'read' : 'unread'}`}
                  onClick={() => {
                    markAsRead(notif.id);
                    if (notif.relatedTicketId) {
                      navigate(`/ticket/${notif.relatedTicketId}`);
                      setShowNotifications(false);
                    }
                  }}
                >
                  <strong>{notif.title}</strong>
                  <p>{notif.message}</p>
                </div>
              ))
            )}
          </div>
        )}

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

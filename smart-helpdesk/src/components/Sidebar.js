import { Link, useLocation } from 'react-router-dom';
import './Sidebar.css';

function Sidebar({ role }) {
  const location = useLocation();

  const menuItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊', roles: ['ADMIN', 'EMPLOYEE', 'CUSTOMER'] },
    { path: '/issues', label: 'All Issues', icon: '🎫', roles: ['ADMIN', 'EMPLOYEE'] },
    { path: '/my-issues', label: 'My Issues', icon: '📝', roles: ['CUSTOMER'] },
    { path: '/users', label: 'Users', icon: '👥', roles: ['ADMIN'] },
    { path: '/reports', label: 'Reports', icon: '📈', roles: ['ADMIN'] },
    { path: '/settings', label: 'Settings', icon: '⚙️', roles: ['ADMIN', 'EMPLOYEE', 'CUSTOMER'] }
  ];

  const filteredMenu = menuItems.filter(item => item.roles.includes(role));

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2 className="sidebar-logo">🛠️ HelpDesk</h2>
      </div>

      <nav className="sidebar-nav">
        {filteredMenu.map(item => (
          <Link
            key={item.path}
            to={item.path}
            className={`sidebar-link ${location.pathname === item.path ? 'active' : ''}`}
          >
            <span className="sidebar-icon">{item.icon}</span>
            <span className="sidebar-label">{item.label}</span>
          </Link>
        ))}
      </nav>
    </div>
  );
}

export default Sidebar;

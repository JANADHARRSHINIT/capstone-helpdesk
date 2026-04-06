import { Link, useLocation } from 'react-router-dom';
import './Sidebar.css';

function Sidebar({ role }) {
  const location = useLocation();
  const permissions = JSON.parse(localStorage.getItem('permissions') || '{}');

  const menuItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '\u{1F4CA}', roles: ['ADMIN', 'EMPLOYEE', 'USER'] },
    { path: '/issues', label: role === 'EMPLOYEE' ? 'My Queue' : 'All Issues', icon: '\u{1F3AB}', roles: ['ADMIN', 'EMPLOYEE'] },
    { path: '/raise-ticket', label: 'Raise Ticket', icon: '\u2795', roles: ['USER'], permission: 'RAISE_TICKET' },
    { path: '/my-issues', label: 'My Issues', icon: '\u{1F4DD}', roles: ['USER'] },
    { path: '/users', label: 'Users', icon: '\u{1F465}', roles: ['ADMIN'] },
    { path: '/reports', label: 'Reports', icon: '\u{1F4C8}', roles: ['ADMIN'] },
    { path: '/audit-logs', label: 'Audit Logs', icon: '\u{1F4CB}', roles: ['ADMIN'] },
    { path: '/settings', label: 'Settings', icon: '\u2699\uFE0F', roles: ['ADMIN', 'EMPLOYEE', 'USER'] }
  ];

  const filteredMenu = menuItems.filter(
    (item) => item.roles.includes(role) && (!item.permission || permissions[item.permission] !== false)
  );

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2 className="sidebar-logo">&#128736;&#65039; HelpDesk</h2>
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

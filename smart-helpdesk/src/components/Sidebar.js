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
    { path: '/settings', label: 'Settings', icon: '\u2699\uFE0F', roles: ['ADMIN', 'EMPLOYEE', 'USER'] }
  ];

  const filteredMenu = menuItems.filter(
    (item) => item.roles.includes(role) && (!item.permission || permissions[item.permission] !== false)
  );
  const modules = [
    { key: 'RAISE_TICKET', label: 'Raise Ticket' },
    { key: 'SELF_SERVICE_TOOLS', label: 'Self-Service Tools' },
    { key: 'SMART_SUGGESTIONS', label: 'Smart Suggestions' },
    { key: 'MODEL_RETRAINING', label: 'Model Re-training' },
    { key: 'FRAUD_ALERTS', label: 'Fraud Alerts' }
  ];

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

      <div className="sidebar-modules">
        <p className="sidebar-modules-title">Module Access</p>
        {modules.map((module) => (
          <div key={module.key} className={`sidebar-module ${permissions[module.key] ? 'enabled' : 'disabled'}`}>
            <span>{permissions[module.key] ? 'ON' : 'OFF'}</span>
            <small>{module.label}</small>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Sidebar;

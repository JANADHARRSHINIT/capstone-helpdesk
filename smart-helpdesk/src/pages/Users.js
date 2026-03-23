import { useCallback, useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { fetchUsers } from '../services/api';
import './Users.css';

function Users() {
  const user = JSON.parse(localStorage.getItem('user'));
  const [users, setUsers] = useState([]);
  const [filter, setFilter] = useState('ALL');

  const loadUsers = useCallback(async () => {
    try {
      const data = await fetchUsers(filter === 'ALL' ? null : filter);
      setUsers(data);
    } catch (error) {
      alert(error.message || 'Failed to load users');
    }
  }, [filter]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const getRoleBadge = (role) => {
    if (role === 'ADMIN') return 'badge-high';
    if (role === 'EMPLOYEE') return 'badge-medium';
    return 'badge-low';
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      
      <div className="dashboard-main">
        <Navbar title="User Management" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="issues-header">
            <div className="stats-summary">
              <span>Total Users: {users.length}</span>
            </div>
            <select value={filter} onChange={(e) => setFilter(e.target.value)} className="filter-select">
              <option value="ALL">All Roles</option>
              <option value="ADMIN">Admin</option>
              <option value="EMPLOYEE">Employee</option>
              <option value="USER">User</option>
            </select>
          </div>

          <div className="issues-table-container">
            <table className="issues-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Team</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id}>
                    <td>#{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td><span className={`badge ${getRoleBadge(u.role)}`}>{u.role}</span></td>
                    <td>{u.team || 'N/A'}</td>
                    <td><span className="badge badge-resolved">Active</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Users;

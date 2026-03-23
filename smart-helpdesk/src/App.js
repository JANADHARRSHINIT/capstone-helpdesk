import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Issues from './pages/Issues';
import MyIssues from './pages/MyIssues';
import RaiseTicket from './pages/RaiseTicket';
import TicketDetail from './pages/TicketDetail';
import Settings from './pages/Settings';
import Users from './pages/Users';
import Reports from './pages/Reports';
import ProtectedRoute from './components/ProtectedRoute';
import './styles/global.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/" element={<Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={<ProtectedRoute allowedRoles={['ADMIN', 'EMPLOYEE', 'USER']}><Dashboard /></ProtectedRoute>} />
        <Route path="/analytics" element={<ProtectedRoute allowedRoles={['ADMIN', 'EMPLOYEE', 'USER']}><Dashboard /></ProtectedRoute>} />
        <Route path="/issues" element={<ProtectedRoute allowedRoles={['ADMIN', 'EMPLOYEE']}><Issues /></ProtectedRoute>} />
        <Route path="/raise-ticket" element={<ProtectedRoute allowedRoles={['USER']} requiredPermission="RAISE_TICKET"><RaiseTicket /></ProtectedRoute>} />
        <Route path="/my-issues" element={<ProtectedRoute allowedRoles={['USER']}><MyIssues /></ProtectedRoute>} />
        <Route path="/ticket/:id" element={<ProtectedRoute allowedRoles={['ADMIN', 'EMPLOYEE', 'USER']}><TicketDetail /></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute allowedRoles={['ADMIN']}><Users /></ProtectedRoute>} />
        <Route path="/reports" element={<ProtectedRoute allowedRoles={['ADMIN']}><Reports /></ProtectedRoute>} />
        <Route path="/settings" element={<ProtectedRoute allowedRoles={['ADMIN', 'EMPLOYEE', 'USER']}><Settings /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/dashboard" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

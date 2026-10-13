import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import './App.css';

import RootRoute from './components/RootRoute';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import MainLayout from './layouts/MainLayout';
import Home from './pages/admin/Home';
import Users from './pages/admin/Users';
import Roles from './pages/admin/Roles';
import Positions from './pages/admin/Positions';
import Resources from './pages/admin/Resources';
import Connectors from './pages/admin/Connectors';
import Permissions from './pages/admin/Permissions';
import AccessLogs from './pages/admin/AccessLogs';
import AuditLogs from './pages/admin/AuditLogs';

import Portal from './pages/user/Portal';
import Overview from './pages/Overview';

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Overview />} />
                <Route path="/login" element={<Login />} />
                <Route path="/portal" element={<Portal />} />
                <Route path="/app" element={<ProtectedRoute />}>
                    <Route element={<MainLayout />}>
                        <Route path="home" element={<Home />} />
                        <Route path="users" element={<Users />} />
                        <Route path="roles" element={<Roles />} />
                        <Route path="positions" element={<Positions />} />
                        <Route path="resources" element={<Resources />} />
                        <Route path="connectors" element={<Connectors />} />
                        <Route path="permissions" element={<Permissions />} />
                        <Route path="logs/access" element={<AccessLogs />} />
                        <Route path="logs/audit" element={<AuditLogs />} />
                    </Route>
                </Route>
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
};

export default App;
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import './App.css';

import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import AdminLayout from './layouts/AdminLayout';
import Home from './pages/admin/Home';
import Users from './pages/admin/Users';

import Portal from './pages/user/Portal';
import Overview from './pages/Overview';
import UserLayout from './layouts/UserLayout';

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Overview />} />
                <Route path="/login" element={<Login />} />

                {/* Đưa Portal vào trong ProtectedRoute, và chỉ cho phép USER */}
                <Route element={<ProtectedRoute allowedRoles={['USER']} />}>
                    <Route element={<UserLayout />}>
                        <Route path="/portal" element={<Portal />} />
                    </Route>

                </Route>

                {/* Khu vực Admin, chỉ cho phép ADMIN */}
                <Route path="/app" element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                    <Route element={<AdminLayout />}>
                        <Route path="home" element={<Home />} />
                        <Route path="users" element={<Users />} />
                        {/* ... các route con khác ... */}
                    </Route>
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter >
    );
};

export default App;
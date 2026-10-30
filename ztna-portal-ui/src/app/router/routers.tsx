import { createBrowserRouter, Navigate } from 'react-router-dom';

import ProtectedRoute from './ProtectedRoute';

import PublicLayout from '@/app/layouts/PublicLayout';
import AdminLayout from '@/app/layouts/AdminLayout';
import UserLayout from '@/app/layouts/UserLayout';

import { OverviewPage } from '@/features/landing';
import { LoginPage } from '@/features/auth';
import { PortalPage } from '@/features/portal';
import { Dashboard } from '@/features/dashboard';
import { ReportsPage } from '@/features/reports';
import { ResourcesPage } from '@/features/resources';
import { NetworkLogsPage } from '@/features/network-log';
import { UsersPage, RolesPage } from '@/features/identity';
import { PermissionsPage } from '@/features/authorization';

export const router = createBrowserRouter([
    {
        element: <PublicLayout />,
        children: [
            { path: '/', element: <OverviewPage /> },
            { path: '/login', element: <LoginPage /> },
        ]
    },

    {
        element: <ProtectedRoute allowedRoles={['USER', 'SUPERVISOR']} />,
        children: [
            {
                element: <UserLayout />,
                children: [
                    { path: '/portal', element: <PortalPage /> },
                ]
            }
        ]
    },

    {
        element: <ProtectedRoute allowedRoles={['ADMIN']} />,
        children: [
            {
                element: <AdminLayout />,
                children: [
                    { index: true, element: <Navigate to="dashboard" replace /> },
                    { path: 'dashboard', element: <Dashboard /> },
                    { path: 'reports', element: <ReportsPage /> },
                    { path: 'resources', element: <ResourcesPage /> },
                    { path: 'network-logs', element: <NetworkLogsPage /> },
                    { path: 'users', element: <UsersPage /> },
                    { path: 'roles', element: <RolesPage /> },
                    { path: 'permissions', element: <PermissionsPage key="all" scope="all" /> },
                    { path: 'permissions/roles', element: <PermissionsPage key="role" scope="role" /> },
                    { path: 'permissions/users', element: <PermissionsPage key="user" scope="user" /> },
                    { path: 'permissions/resources', element: <PermissionsPage key="resource" scope="resource" /> },
                    { path: 'permissions/expiring', element: <PermissionsPage key="expiring" scope="expiring" /> },
                    // ... các route con khác
                ]
            }
        ]
    },

    { path: '*', element: <Navigate to="/" replace /> }
]);

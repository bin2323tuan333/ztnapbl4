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
import { ConnectorsPage } from '@/features/connectors';
import { RulesPage } from '@/features/rules';

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
                    { path: 'connectors', element: <ConnectorsPage /> },
                    { path: 'rules', element: <RulesPage /> },
                ]
            }
        ]
    },

    { path: '*', element: <Navigate to="/" replace /> }
]);

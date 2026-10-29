import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import './styles/AdminLayout.css';

const AdminLayout = () => {
    const [collapsed, setCollapsed] = useState(false);

    return (
        <div className="ml-layout">
            <Header />
            <div className="ml-body">
                <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(!collapsed)} />
                <main className="ml-content">
                    <div className="ml-content-inner">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;
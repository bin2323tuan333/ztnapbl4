import { Outlet } from 'react-router-dom';
import Header from './components/Header';
import './styles/UserLayout.css';

const UserLayout = () => {
    return (
        <div className="ml-layout">
            <Header />
            <div className="ml-content">
                <div className="ml-content-inner">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default UserLayout;
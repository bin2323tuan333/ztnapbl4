import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, ChevronDown, LogOut } from 'lucide-react';
import './Header.css';

const Header: React.FC = () => {
    const navigate = useNavigate();
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const username = localStorage.getItem('username') || 'Admin';

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        navigate('/');
    };

    return (
        <header className="hd-header">
            <div className="hd-left">
                <div className="hd-logo-icon">
                    <Shield size={24} />
                </div>
                <span className="hd-title">ZTNA System</span>
            </div>
            <div className="hd-right" ref={dropdownRef}>
                <div className="hd-user" onClick={() => setDropdownOpen(!dropdownOpen)}>
                    <div className="hd-avatar">
                        {username.charAt(0).toUpperCase()}
                    </div>
                    <span className="hd-username">{username}</span>
                    <ChevronDown size={16} className={`hd-chevron ${dropdownOpen ? 'hd-chevron-open' : ''}`} />
                </div>
                {dropdownOpen && (
                    <div className="hd-dropdown">
                        <button className="hd-dropdown-item" onClick={handleLogout}>
                            <LogOut size={16} />
                            <span>Đăng xuất</span>
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
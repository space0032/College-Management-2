import React from 'react';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();
  const user = (() => {
    try {
      return JSON.parse(localStorage.getItem('user') || '{}');
    } catch {
      return {};
    }
  })();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <header className="header">
      <span className="header-title">College Management System</span>
      <div className="header-right">
        <div className="header-user">
          <span className="header-user-name">{user.username || user.name || 'User'}</span>
          <span className="header-user-role">{user.role || 'Guest'}</span>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
          🚪 Logout
        </button>
      </div>
    </header>
  );
};

export default Header;

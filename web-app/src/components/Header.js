import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import GlobalSearch from './GlobalSearch';
import SessionManager from '../utils/SessionManager';

const Header = ({ onMenuClick }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [showSearch, setShowSearch] = useState(false);
  const user = SessionManager.getUser() || {};

  React.useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setShowSearch(true);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleLogout = () => {
    SessionManager.clearSession();
    navigate('/');
  };

  const pageName = location.pathname === '/dashboard'
    ? 'Overview'
    : location.pathname.split('/').filter(Boolean).pop()?.replace(/-/g, ' ');
  const displayName = user.name || user.username || 'User';
  const initials = displayName.split(/\s+/).map(part => part[0]).join('').slice(0, 2).toUpperCase();

  return (
    <>
      <header className="header">
        <div className="header-heading">
          <button className="menu-button" onClick={onMenuClick} aria-label="Open navigation">☰</button>
          <div>
            <span className="header-eyebrow">Workspace</span>
            <h1 className="header-title">{pageName || 'Overview'}</h1>
          </div>
        </div>
        <div className="header-right">
          {/* Global Search Button */}
          <button
            className="header-search"
            onClick={() => setShowSearch(true)}
            title="Search (Ctrl+K)"
          >
            <span aria-hidden="true">⌕</span><span>Search anything</span><kbd>Ctrl K</kbd>
          </button>
          <div className="header-divider" />
          <div className="user-avatar" aria-hidden="true">{initials}</div>
          <div className="header-user">
            <span className="header-user-name">{displayName}</span>
            <span className="header-user-role">{user.role || 'Guest'}</span>
          </div>
          <button className="icon-button logout-button" onClick={handleLogout} title="Sign out" aria-label="Sign out">
            ↗
          </button>
        </div>
      </header>
      {showSearch && <GlobalSearch onClose={() => setShowSearch(false)} />}
    </>
  );
};

export default Header;

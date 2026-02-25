import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import GlobalSearch from './GlobalSearch';

const Header = () => {
  const navigate = useNavigate();
  const [showSearch, setShowSearch] = useState(false);
  const user = (() => {
    try {
      return JSON.parse(localStorage.getItem('user') || '{}');
    } catch {
      return {};
    }
  })();

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
    localStorage.clear();
    navigate('/');
  };

  return (
    <>
      <header className="header">
        <span className="header-title">College Management System</span>
        <div className="header-right">
          {/* Global Search Button */}
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => setShowSearch(true)}
            title="Search (Ctrl+K)"
            style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '6px 12px' }}
          >
            🔍 <span style={{ fontSize: '0.8rem', opacity: 0.75 }}>Search...</span>
          </button>
          <div className="header-user">
            <span className="header-user-name">{user.username || user.name || 'User'}</span>
            <span className="header-user-role">{user.role || 'Guest'}</span>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
            🚪 Logout
          </button>
        </div>
      </header>
      {showSearch && <GlobalSearch onClose={() => setShowSearch(false)} />}
    </>
  );
};

export default Header;

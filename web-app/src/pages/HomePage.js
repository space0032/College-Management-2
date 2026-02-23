import React, { useEffect, useState } from 'react';
import { getAnnouncements } from '../services/announcementService';

const STATS = [
  { icon: '🎓', label: 'Total Students', value: '—', color: '#0366d6' },
  { icon: '👩‍🏫', label: 'Total Faculty', value: '—', color: '#28a745' },
  { icon: '📚', label: 'Active Courses', value: '—', color: '#e36209' },
  { icon: '🏛️', label: 'Departments', value: '—', color: '#6f42c1' },
];

const HomePage = () => {
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
  })();

  useEffect(() => {
    getAnnouncements()
      .then((res) => setAnnouncements(res.data?.slice(0, 5) || []))
      .catch(() => setAnnouncements([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <div className="welcome-banner">
        <h2>Welcome back, {user.username || user.name || 'User'}! 👋</h2>
        <p>Here's an overview of your college management system.</p>
      </div>

      <div className="stats-grid">
        {STATS.map((stat) => (
          <div className="stat-card" key={stat.label}>
            <div className="stat-card-icon">{stat.icon}</div>
            <div className="stat-card-value" style={{ color: stat.color }}>{stat.value}</div>
            <div className="stat-card-label">{stat.label}</div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-header">
          <h3>📢 Recent Announcements</h3>
        </div>
        <div className="card-body">
          {loading ? (
            <div className="loading-container"><div className="spinner" /></div>
          ) : announcements.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              No announcements at this time.
            </p>
          ) : (
            <ul style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {announcements.map((a, i) => (
                <li key={a.id ?? i} style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px' }}>
                  <div style={{ fontWeight: 600, marginBottom: '2px' }}>{a.title}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{a.content}</div>
                  {a.createdAt && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                      {new Date(a.createdAt).toLocaleDateString()}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

export default HomePage;

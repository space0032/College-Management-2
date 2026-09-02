import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAnnouncements } from '../services/announcementService';
import { getAuditLogs } from '../services/auditService';
import SessionManager from '../utils/SessionManager';

const STATS_CONFIG = [
  {
    key: 'totalStudents', icon: '🎓', label: 'Total Students',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trendKey: 'studentsThisWeek', trendLabel: 'enrolled this week',
    route: '/dashboard/students'
  },
  {
    key: 'totalFaculty', icon: '👩‍🏫', label: 'Total Faculty',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    trendKey: 'facultyThisWeek', trendLabel: 'new this week',
    route: '/dashboard/faculty'
  },
  {
    key: 'activeCourses', icon: '📚', label: 'Active Courses',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    trendKey: 'coursesThisWeek', trendLabel: 'added this week',
    route: '/dashboard/courses'
  },
  {
    key: 'departments', icon: '🏛️', label: 'Departments',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    trendKey: null, trendLabel: null,
    route: '/dashboard/departments'
  },
];

const PRIORITY_COLORS = { HIGH: '#e53e3e', MEDIUM: '#dd6b20', LOW: '#38a169', NORMAL: '#3182ce' };

const HomePage = () => {
  const navigate = useNavigate();
  const [announcements, setAnnouncements] = useState([]);
  const [recentActivity, setRecentActivity] = useState([]);
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [activeTab, setActiveTab] = useState('announcements');

  const user = SessionManager.getUser() || {};

  useEffect(() => {
    const loadDashboard = async () => {
      setLoadError('');
      try {
        const [{ getDashboardStats }, ann, audit] = await Promise.all([
          import('../services/dashboardService'),
          getAnnouncements(),
          getAuditLogs({ limit: 20 })
        ]);
        const statsResponse = await getDashboardStats();
        setStats(statsResponse.data || {});
        setAnnouncements((Array.isArray(ann.data) ? ann.data : []).slice(0, 8));
        setRecentActivity((Array.isArray(audit.data) ? audit.data : []).slice(0, 15));
      } catch (error) {
        setLoadError(error.response?.data?.error || 'Dashboard data could not be loaded. Please retry.');
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, []);

  const ACTION_ICONS = { CREATE: '✅', LOGIN: '🔑', DELETE: '🗑️', UPDATE: '✏️', LOGOUT: '🚪' };
  const getActionIcon = (action) => {
    const key = Object.keys(ACTION_ICONS).find(k => (action || '').toUpperCase().includes(k));
    return key ? ACTION_ICONS[key] : '📋';
  };

  const formatTime = (ts) => {
    if (!ts) return '';
    try {
      const d = new Date(ts);
      const now = new Date();
      const diff = Math.floor((now - d) / 60000);
      if (diff < 1) return 'just now';
      if (diff < 60) return `${diff}m ago`;
      if (diff < 1440) return `${Math.floor(diff / 60)}h ago`;
      return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
    } catch { return ''; }
  };

  const tabStyle = (t) => ({
    padding: '8px 18px', border: 'none', cursor: 'pointer',
    background: 'none', fontWeight: activeTab === t ? '600' : '400',
    borderBottom: activeTab === t ? '2px solid #3b82f6' : '2px solid transparent',
    color: activeTab === t ? '#3b82f6' : '#718096', fontSize: '0.88rem',
    transition: 'all 0.15s'
  });

  return (
    <div>
      {loadError && (
        <div className="alert alert-error" role="alert" style={{ marginBottom: '16px' }}>
          {loadError}
        </div>
      )}
      {/* Welcome Banner */}
      <div style={{
        background: 'linear-gradient(135deg, #1a365d 0%, #2a69ac 100%)',
        borderRadius: '14px', padding: '28px 32px', marginBottom: '28px',
        color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px'
      }}>
        <div>
          <h2 style={{ margin: 0, fontSize: '1.6rem', fontWeight: '700' }}>
            Welcome back, {user.name || user.username || 'User'}! 👋
          </h2>
          <p style={{ margin: '6px 0 0', opacity: 0.8, fontSize: '0.9rem' }}>
            {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          {user.role === 'STUDENT' && (
            <button onClick={() => navigate('/dashboard/learning')} style={{
              background: 'rgba(255,255,255,0.15)', border: '1px solid rgba(255,255,255,0.3)',
              color: 'white', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '0.85rem'
            }}>🎓 Learning Portal</button>
          )}
          <button onClick={() => navigate('/dashboard/profile')} style={{
            background: 'rgba(255,255,255,0.15)', border: '1px solid rgba(255,255,255,0.3)',
            color: 'white', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '0.85rem'
          }}>👤 My Profile</button>
        </div>
      </div>

      {/* Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '28px' }}>
        {STATS_CONFIG.map((stat) => {
          const value = stats[stat.key];
          const trend = stat.trendKey ? stats[stat.trendKey] : null;
          return (
            <div
              key={stat.label}
              onClick={() => navigate(stat.route)}
              style={{
                background: 'white', borderRadius: '12px', padding: '20px',
                border: '1px solid #e2e8f0', cursor: 'pointer', overflow: 'hidden',
                position: 'relative', transition: 'transform 0.15s, box-shadow 0.15s'
              }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}
            >
              {/* Accent bar */}
              <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '4px', background: stat.gradient }} />
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginTop: '4px' }}>
                <div>
                  <div style={{ fontSize: '2rem', fontWeight: '800', color: '#1a202c', lineHeight: 1 }}>
                    {value !== undefined ? value : '—'}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: '#718096', marginTop: '6px' }}>{stat.label}</div>
                  {trend != null && trend > 0 && (
                    <div style={{ marginTop: '8px', fontSize: '0.75rem', color: '#38a169', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '3px' }}>
                      <span>▲</span><span>+{trend} {stat.trendLabel}</span>
                    </div>
                  )}
                </div>
                <div style={{
                  width: '44px', height: '44px', borderRadius: '10px',
                  background: stat.gradient, display: 'flex', alignItems: 'center',
                  justifyContent: 'center', fontSize: '1.3rem', flexShrink: 0, opacity: 0.9
                }}>
                  {stat.icon}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Role-specific quick actions & Finance Activity */}
      <div style={{ display: 'grid', gridTemplateColumns: SessionManager.hasRole('ADMIN') ? '1fr 1.5fr' : '1fr', gap: '28px', marginBottom: '28px' }}>
        {/* Quick Actions Column */}
        <div>
          <h3 style={{ fontSize: '1rem', fontWeight: '700', marginBottom: '16px', color: '#4a5568', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ⚡ Professional Quick Actions
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px' }}>
            {SessionManager.hasRole('ADMIN') && (
              <>
                <button onClick={() => navigate('/dashboard/payroll')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>💸</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Process Payroll</span>
                </button>
                <button onClick={() => navigate('/dashboard/management')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>⚙️</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Sys Admin</span>
                </button>
                <button onClick={() => navigate('/dashboard/reports')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>📈</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Gen Reports</span>
                </button>
                <button onClick={() => navigate('/dashboard/settings')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>🎨</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Branding</span>
                </button>
              </>
            )}
            {user.role === 'FACULTY' && (
              <>
                <button onClick={() => navigate('/dashboard/attendance')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>🙋</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Mark Attd</span>
                </button>
                <button onClick={() => navigate('/dashboard/grades')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>📖</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Post Marks</span>
                </button>
              </>
            )}
            {user.role === 'STUDENT' && (
              <>
                <button onClick={() => navigate('/dashboard/fees')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>💳</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Pay Fees</span>
                </button>
                <button onClick={() => navigate('/dashboard/learning')} className="btn btn-secondary" style={{ padding: '16px', background: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '1.5rem' }}>📓</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Resources</span>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Finance / Important Stats Column */}
        {SessionManager.hasRole('ADMIN') && (
          <div>
            <h3 style={{ fontSize: '1rem', fontWeight: '700', marginBottom: '16px', color: '#4a5568', display: 'flex', alignItems: 'center', gap: '8px' }}>
              💰 Finance Activity & Indicators
            </h3>
            <div className="stat-card" style={{ padding: '0', overflow: 'hidden' }}>
              <div style={{ padding: '20px', borderBottom: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Projected Revenue (Term)</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: '800', color: '#10b981' }}>
                    ₹{stats.projectedRevenue ? (stats.projectedRevenue >= 10000000 ? (stats.projectedRevenue / 10000000).toFixed(2) + ' Cr' : (stats.projectedRevenue / 100000).toFixed(1) + ' L') : '0.00'}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Collection Rate</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: '800', color: '#6366f1' }}>{stats.collectionRate || 0}%</div>
                </div>
              </div>
              <div style={{ padding: '15px' }}>
                <div style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '10px' }}>Recent Collections</div>
                {(!stats.recentCollections || stats.recentCollections.length === 0) ? (
                  <div style={{ padding: '20px', textAlign: 'center', color: '#a0aec0', fontSize: '0.85rem' }}>No recent payments</div>
                ) : stats.recentCollections.map((item, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: i === stats.recentCollections.length - 1 ? 'none' : '1px solid #f8fafc', fontSize: '0.85rem' }}>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                      <span style={{ color: '#475569', fontWeight: '500' }}>{item.studentName}</span>
                      <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>{item.categoryName}</span>
                    </div>
                    <span style={{ fontWeight: '600', color: '#10b981', alignSelf: 'center' }}>+₹{(item.amount || 0).toLocaleString('en-IN')}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Unified Activity Feed */}
      <div style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <div style={{ borderBottom: '1px solid #e2e8f0', display: 'flex', padding: '0 16px' }}>
          <button style={tabStyle('announcements')} onClick={() => setActiveTab('announcements')}>
            📢 Announcements {announcements.length > 0 && `(${announcements.length})`}
          </button>
          <button style={tabStyle('activity')} onClick={() => setActiveTab('activity')}>
            🕐 Recent Activity
          </button>
        </div>

        <div style={{ padding: '16px', maxHeight: '400px', overflowY: 'auto' }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '30px', color: '#a0aec0' }}>Loading...</div>
          ) : activeTab === 'announcements' ? (
            announcements.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '30px', color: '#a0aec0' }}>
                <div style={{ fontSize: '2rem', marginBottom: '8px' }}>📢</div>
                No announcements yet.
              </div>
            ) : announcements.map((a, i) => {
              const priorityColor = PRIORITY_COLORS[a.priority?.toUpperCase()] || PRIORITY_COLORS.NORMAL;
              return (
                <div key={a.id ?? i} style={{
                  display: 'flex', gap: '14px', padding: '14px 0',
                  borderBottom: i < announcements.length - 1 ? '1px solid #f7fafc' : 'none'
                }}>
                  <div style={{
                    width: '36px', height: '36px', borderRadius: '50%',
                    background: `${priorityColor}15`, display: 'flex',
                    alignItems: 'center', justifyContent: 'center', fontSize: '1rem', flexShrink: 0
                  }}>📢</div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '3px', flexWrap: 'wrap' }}>
                      <span style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.9rem' }}>{a.title}</span>
                      {a.priority && a.priority !== 'NORMAL' && (
                        <span style={{ background: `${priorityColor}15`, color: priorityColor, padding: '1px 7px', borderRadius: '10px', fontSize: '0.7rem', fontWeight: '600' }}>
                          {a.priority}
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: '0.82rem', color: '#718096', lineHeight: '1.4' }}>{a.content}</div>
                    {a.createdAt && (
                      <div style={{ fontSize: '0.73rem', color: '#a0aec0', marginTop: '4px' }}>
                        {formatTime(a.createdAt)}
                      </div>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            recentActivity.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '30px', color: '#a0aec0' }}>
                <div style={{ fontSize: '2rem', marginBottom: '8px' }}>🕐</div>
                No recent activity found.
              </div>
            ) : recentActivity.map((log, i) => (
              <div key={log.id ?? i} style={{
                display: 'flex', gap: '14px', padding: '12px 0',
                borderBottom: i < recentActivity.length - 1 ? '1px solid #f7fafc' : 'none'
              }}>
                <div style={{
                  width: '36px', height: '36px', borderRadius: '50%',
                  background: '#ebf8ff', display: 'flex', alignItems: 'center',
                  justifyContent: 'center', fontSize: '1rem', flexShrink: 0
                }}>
                  {getActionIcon(log.action)}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '2px' }}>
                    <span style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.88rem' }}>
                      {log.username || 'System'}
                    </span>
                    <span style={{ fontSize: '0.72rem', color: '#a0aec0', flexShrink: 0, marginLeft: '8px' }}>
                      {formatTime(log.timestamp)}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.82rem', color: '#718096' }}>
                    <span style={{ fontWeight: '500', color: '#4a5568' }}>{log.action}</span>
                    {log.entityType && <span style={{ marginLeft: '4px' }}>on {log.entityType}</span>}
                    {log.details && <span style={{ marginLeft: '4px' }}>– {log.details.slice(0, 60)}{log.details.length > 60 ? '…' : ''}</span>}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div style={{ borderTop: '1px solid #f0f0f0', padding: '10px 16px', display: 'flex', gap: '12px' }}>
          <button
            onClick={() => navigate('/dashboard/announcements')}
            style={{ background: 'none', border: 'none', color: '#3b82f6', fontSize: '0.82rem', cursor: 'pointer', padding: 0 }}
          >
            View all announcements →
          </button>
          {SessionManager.hasRole('ADMIN') && (
            <button
              onClick={() => navigate('/dashboard/audit')}
              style={{ background: 'none', border: 'none', color: '#718096', fontSize: '0.82rem', cursor: 'pointer', padding: 0 }}
            >
              Full audit log →
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default HomePage;

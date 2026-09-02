import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getEvents } from '../services/eventService';
import { getAnnouncements } from '../services/announcementService';

const MODULES = [
    {
        title: 'Events & Workshops', icon: '🎪', path: '/dashboard/events',
        color: '#3182ce', desc: 'Register for upcoming events, seminars, and hackathons.',
        gradient: 'linear-gradient(135deg, #667eea, #3182ce)'
    },
    {
        title: 'Clubs & Societies', icon: '👥', path: '/dashboard/clubs',
        color: '#38a169', desc: 'Join academic, cultural, and sports clubs.',
        gradient: 'linear-gradient(135deg, #38a169, #68d391)'
    },
    {
        title: 'Volunteer Tasks', icon: '🤝', path: '/dashboard/volunteer',
        color: '#d69e2e', desc: 'Sign up for volunteer opportunities and track hours.',
        gradient: 'linear-gradient(135deg, #d69e2e, #f6e05e)'
    },
    {
        title: 'Scholarships', icon: '💰', path: '/dashboard/scholarships',
        color: '#9f7aea', desc: 'Explore and apply for merit and need-based awards.',
        gradient: 'linear-gradient(135deg, #9f7aea, #b794f4)'
    },
    {
        title: 'Placements', icon: '💼', path: '/dashboard/placements',
        color: '#e53e3e', desc: 'Browse drives, track applications, and interview schedule.',
        gradient: 'linear-gradient(135deg, #e53e3e, #fc8181)'
    },
    {
        title: 'Learning Portal', icon: '🎓', path: '/dashboard/learning',
        color: '#ed8936', desc: 'Access course syllabi and study materials.',
        gradient: 'linear-gradient(135deg, #ed8936, #f6ad55)'
    },
];

const StudentActivitiesHub = () => {
    const navigate = useNavigate();
    const [upcomingEvents, setUpcomingEvents] = useState([]);
    const [announcements, setAnnouncements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        Promise.all([
            getEvents().then(r => {
                const all = Array.isArray(r.data) ? r.data : [];
                const now = new Date();
                return all
                    .filter(e => new Date(e.startDate || e.date) >= now)
                    .sort((a, b) => new Date(a.startDate || a.date) - new Date(b.startDate || b.date))
                    .slice(0, 4);
            }),
            getAnnouncements().then(r => {
                const all = Array.isArray(r.data) ? r.data : [];
                return all.slice(0, 3);
            })
        ]).then(([events, ann]) => {
            setUpcomingEvents(events);
            setAnnouncements(ann);
        }).catch(err => setError(err.response?.data?.error || 'Student activity data could not be loaded.'))
            .finally(() => setLoading(false));
    }, []);

    const formatDate = (d) => {
        if (!d) return '';
        try {
            return new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
        } catch { return String(d); }
    };

    return (
        <div className="page-container">
            {error && <div className="alert alert-error" role="alert">{error}</div>}
            {/* Hero */}
            <div style={{
                background: 'linear-gradient(135deg, #1a365d 0%, #2f855a 100%)',
                borderRadius: '14px', padding: '32px', marginBottom: '32px',
                color: 'white', textAlign: 'center'
            }}>
                <div style={{ fontSize: '2.5rem', marginBottom: '10px' }}>🎯</div>
                <h1 style={{ margin: '0 0 8px', fontSize: '1.8rem', fontWeight: '700' }}>Student Activities Hub</h1>
                <p style={{ margin: 0, opacity: 0.85, maxWidth: '500px', marginInline: 'auto', fontSize: '0.95rem' }}>
                    Your central portal for extracurriculars, learning, opportunities, and campus life.
                </p>
            </div>

            {/* Activity Module Cards */}
            <h3 style={{ marginBottom: '16px', color: '#2d3748' }}>Explore Modules</h3>
            <div style={{
                display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
                gap: '16px', marginBottom: '36px'
            }}>
                {MODULES.map(m => (
                    <div
                        key={m.path}
                        onClick={() => navigate(m.path)}
                        style={{
                            background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0',
                            padding: '20px', cursor: 'pointer', overflow: 'hidden', position: 'relative',
                            transition: 'transform 0.15s, box-shadow 0.15s'
                        }}
                        onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = '0 10px 25px rgba(0,0,0,0.1)'; }}
                        onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}
                    >
                        <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '4px', background: m.gradient }} />
                        <div style={{
                            width: '48px', height: '48px', borderRadius: '12px',
                            background: m.gradient, display: 'flex', alignItems: 'center',
                            justifyContent: 'center', fontSize: '1.5rem', marginBottom: '14px'
                        }}>{m.icon}</div>
                        <div style={{ fontWeight: '700', color: '#2d3748', marginBottom: '6px', fontSize: '0.95rem' }}>{m.title}</div>
                        <div style={{ fontSize: '0.8rem', color: '#718096', lineHeight: '1.5' }}>{m.desc}</div>
                        <div style={{ marginTop: '12px', fontSize: '0.78rem', color: m.color, fontWeight: '600' }}>
                            Explore →
                        </div>
                    </div>
                ))}
            </div>

            {/* Two-column: Upcoming Events + Announcements */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', flexWrap: 'wrap' }}>
                {/* Upcoming Events */}
                <div style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
                    <div style={{ padding: '16px 20px', borderBottom: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ margin: 0, fontSize: '0.95rem', color: '#2d3748' }}>🎪 Upcoming Events</h3>
                        <button onClick={() => navigate('/dashboard/events')} style={{ background: 'none', border: 'none', color: '#3b82f6', fontSize: '0.8rem', cursor: 'pointer' }}>View all →</button>
                    </div>
                    <div style={{ padding: '8px 0' }}>
                        {loading ? (
                            <div style={{ padding: '20px', textAlign: 'center', color: '#a0aec0', fontSize: '0.85rem' }}>Loading...</div>
                        ) : upcomingEvents.length === 0 ? (
                            <div style={{ padding: '20px', textAlign: 'center', color: '#a0aec0', fontSize: '0.85rem' }}>No upcoming events.</div>
                        ) : upcomingEvents.map((ev, i) => (
                            <div key={ev.id ?? i} style={{
                                display: 'flex', gap: '12px', padding: '12px 20px',
                                borderBottom: i < upcomingEvents.length - 1 ? '1px solid #f7fafc' : 'none',
                                alignItems: 'center'
                            }}>
                                <div style={{
                                    width: '40px', height: '40px', borderRadius: '10px', flexShrink: 0,
                                    background: 'linear-gradient(135deg, #667eea, #764ba2)',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    fontSize: '0.7rem', fontWeight: 'bold', color: 'white', textAlign: 'center', lineHeight: '1.2'
                                }}>
                                    {ev.startDate ? new Date(ev.startDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }).split(' ').join('\n') : '📅'}
                                </div>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <div style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.88rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {ev.name || ev.title}
                                    </div>
                                    <div style={{ fontSize: '0.75rem', color: '#718096' }}>
                                        {ev.location || ev.venue} · {formatDate(ev.startDate || ev.date)}
                                    </div>
                                </div>
                                <span style={{
                                    background: '#ebf8ff', color: '#2b6cb0', padding: '2px 8px',
                                    borderRadius: '10px', fontSize: '0.7rem', fontWeight: '600', flexShrink: 0
                                }}>
                                    {ev.type || ev.eventType || 'Event'}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Recent Announcements */}
                <div style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
                    <div style={{ padding: '16px 20px', borderBottom: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ margin: 0, fontSize: '0.95rem', color: '#2d3748' }}>📢 Announcements</h3>
                        <button onClick={() => navigate('/dashboard/announcements')} style={{ background: 'none', border: 'none', color: '#3b82f6', fontSize: '0.8rem', cursor: 'pointer' }}>View all →</button>
                    </div>
                    <div style={{ padding: '8px 0' }}>
                        {loading ? (
                            <div style={{ padding: '20px', textAlign: 'center', color: '#a0aec0', fontSize: '0.85rem' }}>Loading...</div>
                        ) : announcements.length === 0 ? (
                            <div style={{ padding: '20px', textAlign: 'center', color: '#a0aec0', fontSize: '0.85rem' }}>No announcements.</div>
                        ) : announcements.map((a, i) => (
                            <div key={a.id ?? i} style={{
                                padding: '12px 20px',
                                borderBottom: i < announcements.length - 1 ? '1px solid #f7fafc' : 'none'
                            }}>
                                <div style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.88rem', marginBottom: '3px' }}>{a.title}</div>
                                <div style={{ fontSize: '0.78rem', color: '#718096', lineHeight: '1.4' }}>
                                    {(a.content || '').slice(0, 80)}{a.content?.length > 80 ? '…' : ''}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Start a New Club CTA */}
            <div style={{
                marginTop: '28px', textAlign: 'center', padding: '28px', borderRadius: '12px',
                background: 'linear-gradient(135deg, #f0fff4, #c6f6d5)', border: '1px solid #9ae6b4'
            }}>
                <h3 style={{ marginBottom: '8px', color: '#276749' }}>🌱 Want to start something new?</h3>
                <p style={{ marginBottom: '16px', color: '#4a5568', fontSize: '0.9rem' }}>
                    Submit a proposal to start a new club with at least 5 members and a faculty advisor.
                </p>
                <button className="btn btn-primary" onClick={() => navigate('/dashboard/clubs')} style={{ background: '#38a169' }}>
                    Browse Existing Clubs
                </button>
            </div>
        </div>
    );
};

export default StudentActivitiesHub;

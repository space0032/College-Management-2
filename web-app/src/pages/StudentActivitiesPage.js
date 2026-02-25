import React from 'react';
import { useNavigate } from 'react-router-dom';

const StudentActivitiesHub = () => {
    const navigate = useNavigate();

    const activityModules = [
        {
            title: 'Clubs & Societies',
            description: 'Join, explore, and manage your club memberships across the college. Participate in academic, cultural, and sports communities.',
            icon: '👥',
            path: '/dashboard/clubs',
            color: '#4CAF50'
        },
        {
            title: 'Events & Workshops',
            description: 'Register for upcoming events, hackathons, and seminars. View schedules, speakers, and track your event attendance.',
            icon: '🎪',
            path: '/dashboard/events',
            color: '#2196F3'
        },
        {
            title: 'Sports & Athletics',
            description: 'Check team schedules, sign up for intramural leagues, and book sports facilities.',
            icon: '🏆',
            path: '/dashboard/clubs', // Currently handled under clubs or separate if built later
            color: '#FF9800'
        },
        {
            title: 'Extracurricular Credits',
            description: 'Track your extracurricular participation points required for graduation and special awards.',
            icon: '⭐',
            path: '#', // Placeholder
            color: '#9C27B0'
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header" style={{ marginBottom: '40px', textAlign: 'center' }}>
                <div>
                    <h1 style={{ fontSize: '2.5rem', marginBottom: '10px' }}>🎯 Student Activities Hub</h1>
                    <p className="text-muted" style={{ fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto' }}>
                        Your central portal for exploring extracurriculars, joining communities, and maximizing your campus experience.
                    </p>
                </div>
            </div>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '30px',
                padding: '20px'
            }}>
                {activityModules.map((module, index) => (
                    <div
                        key={index}
                        className="card"
                        style={{
                            padding: '30px',
                            cursor: module.path !== '#' ? 'pointer' : 'default',
                            transition: 'transform 0.2s, box-shadow 0.2s',
                            borderTop: `5px solid ${module.color}`,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            textAlign: 'center',
                            opacity: module.path === '#' ? 0.7 : 1
                        }}
                        onClick={() => {
                            if (module.path !== '#') {
                                navigate(module.path);
                            }
                        }}
                        onMouseEnter={(e) => {
                            if (module.path !== '#') {
                                e.currentTarget.style.transform = 'translateY(-5px)';
                                e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.1)';
                            }
                        }}
                        onMouseLeave={(e) => {
                            if (module.path !== '#') {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.05)';
                            }
                        }}
                    >
                        <div style={{
                            fontSize: '4rem',
                            marginBottom: '20px',
                            background: `${module.color}15`,
                            width: '100px',
                            height: '100px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            borderRadius: '50%'
                        }}>
                            {module.icon}
                        </div>
                        <h2 style={{ fontSize: '1.5rem', marginBottom: '15px' }}>{module.title}</h2>
                        <p className="text-muted" style={{ lineHeight: '1.6' }}>{module.description}</p>

                        {module.path === '#' && (
                            <span style={{
                                marginTop: '15px',
                                padding: '5px 10px',
                                background: '#eee',
                                borderRadius: '15px',
                                fontSize: '0.8rem',
                                color: '#666'
                            }}>
                                Coming Soon
                            </span>
                        )}
                    </div>
                ))}
            </div>

            <div style={{ marginTop: '50px', textAlign: 'center', padding: '30px', background: '#f8f9fa', borderRadius: '10px' }}>
                <h3 style={{ marginBottom: '15px' }}>Want to start a new club?</h3>
                <p className="text-muted" style={{ marginBottom: '20px' }}>Submit a proposal with at least 5 interested students and a faculty advisor.</p>
                <button className="btn btn-primary" onClick={() => navigate('/dashboard/clubs')}>Browse Existing Clubs First</button>
            </div>
        </div>
    );
};

export default StudentActivitiesHub;

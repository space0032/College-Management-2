import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

const ROUTES = [
    { label: 'Home / Dashboard', path: '/dashboard', keywords: ['home', 'dashboard', 'overview'] },
    { label: 'My Profile', path: '/dashboard/profile', keywords: ['profile', 'account', 'me', 'password', 'my info'] },
    { label: 'Students', path: '/dashboard/students', keywords: ['students', 'student', 'enrollment', 'admission'] },
    { label: 'Faculty', path: '/dashboard/faculty', keywords: ['faculty', 'teacher', 'professor', 'staff'] },
    { label: 'Departments', path: '/dashboard/departments', keywords: ['department', 'dept'] },
    { label: 'Courses', path: '/dashboard/courses', keywords: ['course', 'subject', 'curriculum'] },
    { label: 'Attendance', path: '/dashboard/attendance', keywords: ['attendance', 'present', 'absent', 'mark'] },
    { label: 'Grades', path: '/dashboard/grades', keywords: ['grade', 'marks', 'cgpa', 'transcript', 'results'] },
    { label: 'Fees', path: '/dashboard/fees', keywords: ['fee', 'fees', 'payment', 'receipt', 'dues', 'pending'] },
    { label: 'Library', path: '/dashboard/library', keywords: ['library', 'book', 'issue', 'return', 'fine'] },
    { label: 'Hostel', path: '/dashboard/hostel', keywords: ['hostel', 'room', 'accommodation', 'dormitory'] },
    { label: 'Placements', path: '/dashboard/placements', keywords: ['placement', 'job', 'company', 'interview', 'drive', 'career'] },
    { label: 'Events', path: '/dashboard/events', keywords: ['event', 'fest', 'seminar', 'workshop', 'cultural'] },
    { label: 'Clubs', path: '/dashboard/clubs', keywords: ['club', 'society', 'team', 'group'] },
    { label: 'Scholarships', path: '/dashboard/scholarships', keywords: ['scholarship', 'bursary', 'merit', 'financial aid'] },
    { label: 'Gate Pass', path: '/dashboard/gatepass', keywords: ['gate pass', 'gatepass', 'outpass', 'exit', 'outing'] },
    { label: 'Timetable', path: '/dashboard/timetable', keywords: ['timetable', 'schedule', 'time table', 'class schedule'] },
    { label: 'Announcements', path: '/dashboard/announcements', keywords: ['announcement', 'notice', 'news', 'circular'] },
    { label: 'Notifications', path: '/dashboard/notifications', keywords: ['notification', 'alert', 'reminder'] },
    { label: 'Reports', path: '/dashboard/reports', keywords: ['report', 'analytics', 'statistics', 'export'] },
    { label: 'Settings', path: '/dashboard/settings', keywords: ['settings', 'college settings', 'branding', 'configuration'] },
    { label: 'Payroll', path: '/dashboard/payroll', keywords: ['payroll', 'salary', 'payment', 'wages'] },
    { label: 'Employees', path: '/dashboard/employees', keywords: ['employee', 'staff', 'non-teaching'] },
    { label: 'Leave Approvals', path: '/dashboard/leaves', keywords: ['leave', 'absence', 'vacation', 'sick'] },
    { label: 'Faculty Workload', path: '/dashboard/workload', keywords: ['workload', 'load', 'classes', 'faculty load'] },
    { label: 'Room Availability', path: '/dashboard/rooms', keywords: ['room', 'lab', 'classroom', 'available', 'booking'] },
    { label: 'Assignments', path: '/dashboard/assignments', keywords: ['assignment', 'homework', 'submission', 'task'] },
    { label: 'Resources', path: '/dashboard/resources', keywords: ['resource', 'material', 'pdf', 'download', 'study'] },
    { label: 'Syllabus', path: '/dashboard/syllabus', keywords: ['syllabus', 'curriculum', 'course outline'] },
    { label: 'Learning Portal', path: '/dashboard/learning', keywords: ['learning', 'portal', 'study material'] },
    { label: 'Volunteer Tasks', path: '/dashboard/volunteer', keywords: ['volunteer', 'contribution', 'help', 'task'] },
    { label: 'Visitors', path: '/dashboard/visitors', keywords: ['visitor', 'guest', 'entry'] },
    { label: 'Academic Calendar', path: '/dashboard/calendar', keywords: ['calendar', 'holiday', 'exam schedule', 'academic'] },
    { label: 'Crowdfunding', path: '/dashboard/crowdfunding', keywords: ['crowdfunding', 'campaign', 'donate', 'fundraise'] },
    { label: 'Institute Management', path: '/dashboard/management', keywords: ['institute', 'management', 'admin', 'roles', 'permissions'] },
];

const GlobalSearch = ({ onClose }) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const inputRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    useEffect(() => {
        if (!query.trim()) { setResults([]); return; }
        const q = query.toLowerCase();
        const matches = ROUTES.filter(r =>
            r.label.toLowerCase().includes(q) ||
            r.keywords.some(k => k.includes(q) || q.includes(k))
        ).slice(0, 8);
        setResults(matches);
    }, [query]);

    const handleSelect = (path) => {
        onClose();
        navigate(path);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Escape') onClose();
    };

    return (
        <div
            style={{
                position: 'fixed', inset: 0, zIndex: 9999,
                background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(4px)',
                display: 'flex', alignItems: 'flex-start', justifyContent: 'center',
                paddingTop: '80px'
            }}
            onClick={onClose}
        >
            <div
                style={{
                    background: 'white', borderRadius: '14px', width: '100%', maxWidth: '560px',
                    boxShadow: '0 20px 60px rgba(0,0,0,0.3)', overflow: 'hidden'
                }}
                onClick={e => e.stopPropagation()}
            >
                {/* Search Input */}
                <div style={{ display: 'flex', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #e2e8f0' }}>
                    <span style={{ fontSize: '1.2rem', marginRight: '12px', color: '#a0aec0' }}>🔍</span>
                    <input
                        ref={inputRef}
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="Search pages, features, modules…"
                        style={{
                            flex: 1, border: 'none', outline: 'none',
                            fontSize: '1rem', background: 'transparent', color: '#2d3748'
                        }}
                    />
                    <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a0aec0', fontSize: '1.3rem' }}>×</button>
                </div>

                {/* Results */}
                {results.length > 0 ? (
                    <div style={{ maxHeight: '380px', overflowY: 'auto' }}>
                        {results.map((r, i) => (
                            <button
                                key={r.path}
                                onClick={() => handleSelect(r.path)}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '12px',
                                    width: '100%', padding: '12px 18px', textAlign: 'left',
                                    border: 'none', borderBottom: i < results.length - 1 ? '1px solid #f7fafc' : 'none',
                                    background: 'white', cursor: 'pointer', transition: 'background 0.1s'
                                }}
                                onMouseEnter={e => e.currentTarget.style.background = '#f7fafc'}
                                onMouseLeave={e => e.currentTarget.style.background = 'white'}
                            >
                                <span style={{
                                    width: '32px', height: '32px', borderRadius: '8px',
                                    background: '#ebf8ff', display: 'flex', alignItems: 'center',
                                    justifyContent: 'center', fontSize: '1rem', flexShrink: 0
                                }}>→</span>
                                <div>
                                    <div style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.9rem' }}>{r.label}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#a0aec0' }}>{r.path}</div>
                                </div>
                            </button>
                        ))}
                    </div>
                ) : query.trim() ? (
                    <div style={{ padding: '28px', textAlign: 'center', color: '#a0aec0' }}>
                        <div style={{ fontSize: '1.5rem', marginBottom: '8px' }}>🔍</div>
                        No pages found for "<strong>{query}</strong>"
                    </div>
                ) : (
                    <div style={{ padding: '20px 18px' }}>
                        <div style={{ fontSize: '0.75rem', color: '#a0aec0', marginBottom: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                            Quick Access
                        </div>
                        {['Dashboard', 'Attendance', 'Grades', 'Fees', 'Library'].map(name => {
                            const match = ROUTES.find(r => r.label === name || r.label.startsWith(name));
                            if (!match) return null;
                            return (
                                <button key={match.path} onClick={() => handleSelect(match.path)} style={{
                                    display: 'inline-block', margin: '4px', padding: '6px 14px',
                                    border: '1px solid #e2e8f0', borderRadius: '20px', background: 'white',
                                    cursor: 'pointer', fontSize: '0.85rem', color: '#4a5568'
                                }}>
                                    {match.label}
                                </button>
                            );
                        })}
                    </div>
                )}

                {/* Footer hint */}
                <div style={{ padding: '8px 18px', borderTop: '1px solid #f0f0f0', display: 'flex', gap: '16px', fontSize: '0.75rem', color: '#a0aec0' }}>
                    <span>↩ to navigate</span>
                    <span>Esc to close</span>
                </div>
            </div>
        </div>
    );
};

export default GlobalSearch;

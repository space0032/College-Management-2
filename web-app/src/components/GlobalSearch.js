import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import SessionManager from '../utils/SessionManager';

const ROUTES = [
    { label: 'Home / Dashboard', path: '/dashboard', keywords: ['home', 'dashboard', 'overview'] },
    { label: 'My Profile', path: '/dashboard/profile', keywords: ['profile', 'account', 'me', 'password', 'my info'] },
    { label: 'Faculty Portal', path: '/dashboard/faculty-portal', keywords: ['faculty portal', 'portal', 'my classes'], perm: 'VIEW_FACULTY_PORTAL' },
    { label: 'Students', path: '/dashboard/students', keywords: ['students', 'student', 'enrollment', 'admission'], perm: 'VIEW_STUDENT' },
    { label: 'Student Profile', path: '/dashboard/student-profile', keywords: ['student profile', 'academic record', 'transcript'], perm: 'VIEW_STUDENT_PROFILE' },
    { label: 'Faculty', path: '/dashboard/faculty', keywords: ['faculty', 'teacher', 'professor', 'staff'], perm: 'VIEW_FACULTY' },
    { label: 'Departments', path: '/dashboard/departments', keywords: ['department', 'dept'], perm: 'VIEW_DEPARTMENT' },
    { label: 'Tracks', path: '/dashboard/specializations', keywords: ['track', 'specialization', 'specialisation', 'cyber', 'branch'], perm: 'VIEW_DEPARTMENT' },
    { label: 'Courses', path: '/dashboard/courses', keywords: ['course', 'subject', 'curriculum'], perm: 'VIEW_COURSE' },
    { label: 'Course Registration', path: '/dashboard/course-registrations', keywords: ['course registration', 'register', 'elective'], perm: 'VIEW_COURSE' },
    { label: 'Attendance', path: '/dashboard/attendance', keywords: ['attendance', 'present', 'absent', 'mark'], perm: 'VIEW_ATTENDANCE' },
    { label: 'Grades', path: '/dashboard/grades', keywords: ['grade', 'marks', 'cgpa', 'transcript', 'results'], perm: 'VIEW_GRADES' },
    { label: 'Fees', path: '/dashboard/fees', keywords: ['fee', 'fees', 'payment', 'receipt', 'dues', 'pending'], perm: 'VIEW_FEES' },
    { label: 'Library', path: '/dashboard/library', keywords: ['library', 'book', 'issue', 'return', 'fine'], perm: 'VIEW_LIBRARY' },
    { label: 'Book Requests', path: '/dashboard/book-requests', keywords: ['book request', 'request book', 'hold'], perm: 'VIEW_FACULTY' },
    { label: 'Feedback', path: '/dashboard/feedback', keywords: ['feedback', 'review', 'rating'], perm: 'VIEW_FACULTY' },
    { label: 'Hostel', path: '/dashboard/hostel', keywords: ['hostel', 'room', 'accommodation', 'dormitory'], perm: 'VIEW_HOSTEL' },
    { label: 'Hostel Complaints', path: '/dashboard/hostel/complaints', keywords: ['hostel complaint', 'complaint', 'issue report'], perm: 'VIEW_COMPLAINT' },
    { label: 'Hostel Attendance', path: '/dashboard/hostel/attendance', keywords: ['hostel attendance', 'attendance', 'roll call'], perm: 'VIEW_HOSTEL_ATTENDANCE' },
    { label: 'Wardens', path: '/dashboard/wardens', keywords: ['warden', 'hostel staff'], perm: 'MANAGE_HOSTEL' },
    { label: 'Placements', path: '/dashboard/placements', keywords: ['placement', 'job', 'company', 'interview', 'drive', 'career'], perm: 'VIEW_PLACEMENT' },
    { label: 'Events', path: '/dashboard/events', keywords: ['event', 'fest', 'seminar', 'workshop', 'cultural'], perm: 'VIEW_EVENT' },
    { label: 'Activities Hub', path: '/dashboard/activities', keywords: ['activities', 'activity hub', 'extracurricular'], perm: 'VIEW_EVENT' },
    { label: 'Clubs', path: '/dashboard/clubs', keywords: ['club', 'society', 'team', 'group'], perm: 'VIEW_CLUB' },
    { label: 'Scholarships', path: '/dashboard/scholarships', keywords: ['scholarship', 'bursary', 'merit', 'financial aid'], perm: 'VIEW_SCHOLARSHIP' },
    { label: 'Gate Pass', path: '/dashboard/gatepass', keywords: ['gate pass', 'gatepass', 'outpass', 'exit', 'outing'], perm: 'VIEW_GATEPASS' },
    { label: 'Timetable', path: '/dashboard/timetable', keywords: ['timetable', 'schedule', 'time table', 'class schedule'], perm: 'VIEW_TIMETABLE' },
    { label: 'Announcements', path: '/dashboard/announcements', keywords: ['announcement', 'notice', 'news', 'circular'], perm: 'VIEW_ANNOUNCEMENT' },
    { label: 'Notifications', path: '/dashboard/notifications', keywords: ['notification', 'alert', 'reminder'], perm: 'VIEW_NOTIFICATION' },
    { label: 'My Leave', path: '/dashboard/staff-leave', keywords: ['my leave', 'leave request', 'staff leave'], perm: 'VIEW_LEAVE' },
    { label: 'Reports', path: '/dashboard/reports', keywords: ['report', 'analytics', 'statistics', 'export'], perm: 'VIEW_REPORT' },
    { label: 'Settings', path: '/dashboard/settings', keywords: ['settings', 'college settings', 'branding', 'configuration'], perm: 'VIEW_SETTINGS' },
    { label: 'Payroll', path: '/dashboard/payroll', keywords: ['payroll', 'salary', 'payment', 'wages'], perm: 'VIEW_PAYROLL' },
    { label: 'Employees', path: '/dashboard/employees', keywords: ['employee', 'staff', 'non-teaching'], perm: 'VIEW_EMPLOYEE' },
    { label: 'Leave Approvals', path: '/dashboard/leaves', keywords: ['leave', 'absence', 'vacation', 'sick'], perm: 'VIEW_LEAVE' },
    { label: 'Faculty Workload', path: '/dashboard/workload', keywords: ['workload', 'load', 'classes', 'faculty load'], perm: 'VIEW_WORKLOAD' },
    { label: 'Room Availability', path: '/dashboard/rooms', keywords: ['room', 'lab', 'classroom', 'available', 'booking'], perm: 'VIEW_ROOM' },
    { label: 'Assignments', path: '/dashboard/assignments', keywords: ['assignment', 'homework', 'submission', 'task'], perm: 'VIEW_ASSIGNMENT' },
    { label: 'Resources', path: '/dashboard/resources', keywords: ['resource', 'material', 'pdf', 'download', 'study'], perm: 'VIEW_RESOURCES' },
    { label: 'Syllabus', path: '/dashboard/syllabus', keywords: ['syllabus', 'curriculum', 'course outline'], perm: 'VIEW_SYLLABUS' },
    { label: 'Learning Portal', path: '/dashboard/learning', keywords: ['learning', 'portal', 'study material'], perm: 'VIEW_COURSE' },
    { label: 'Volunteer Tasks', path: '/dashboard/volunteer', keywords: ['volunteer', 'contribution', 'help', 'task'], perm: 'VIEW_VOLUNTEER' },
    { label: 'Visitors', path: '/dashboard/visitors', keywords: ['visitor', 'guest', 'entry'], perm: 'VIEW_VISITOR' },
    { label: 'Academic Calendar', path: '/dashboard/calendar', keywords: ['calendar', 'holiday', 'exam schedule', 'academic'], perm: 'VIEW_CALENDAR' },
    { label: 'Crowdfunding', path: '/dashboard/crowdfunding', keywords: ['crowdfunding', 'campaign', 'donate', 'fundraise'], perm: 'VIEW_CROWDFUNDING' },
    { label: 'Institute Management', path: '/dashboard/management', keywords: ['institute', 'management', 'admin', 'roles', 'permissions'] },
    { label: 'Role Management', path: '/dashboard/roles', keywords: ['roles', 'role management', 'permissions'], perm: 'VIEW_ROLE' },
    { label: 'Student Affairs', path: '/dashboard/student-affairs', keywords: ['student affairs', 'disciplinary', 'counseling'], perm: 'VIEW_STUDENT' },
    { label: 'Audit Log', path: '/dashboard/audit', keywords: ['audit', 'audit log', 'activity trail'], perm: 'VIEW_AUDIT' },
];

const isVisible = (route) => !route.perm || SessionManager.hasPermission(route.perm) || SessionManager.hasRole('ADMIN');

const GlobalSearch = ({ onClose }) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [activeIndex, setActiveIndex] = useState(-1);
    const inputRef = useRef(null);
    const listRef = useRef(null);
    const panelRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        inputRef.current?.focus();
        document.body.style.overflow = 'hidden';
        const handleKey = (e) => {
            if (e.key === 'Escape') {
                e.stopPropagation();
                onClose();
            }
        };
        document.addEventListener('keydown', handleKey);
        return () => {
            document.removeEventListener('keydown', handleKey);
            document.body.style.overflow = '';
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        if (!query.trim()) { setResults([]); setActiveIndex(-1); return; }
        const q = query.toLowerCase();
        const matches = ROUTES.filter(r =>
            isVisible(r) &&
            (r.label.toLowerCase().includes(q) ||
            r.keywords.some(k => k.includes(q) || q.includes(k)))
        ).slice(0, 8);
        setResults(matches);
        setActiveIndex(matches.length ? 0 : -1);
    }, [query]);

    const handleSelect = (path) => {
        onClose();
        navigate(path);
    };

    const activeResult = results[activeIndex];

    const handleKeyDown = (e) => {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIndex(prev => (results.length ? (prev + 1) % results.length : -1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIndex(prev => (results.length ? (prev - 1 + results.length) % results.length : -1));
        } else if (e.key === 'Enter' && activeResult) {
            e.preventDefault();
            handleSelect(activeResult.path);
        }
    };

    useEffect(() => {
        if (activeIndex >= 0 && listRef.current) {
            listRef.current.children[activeIndex]?.scrollIntoView({ block: 'nearest' });
        }
    }, [activeIndex]);

    return (
        <div
            role="dialog"
            aria-modal="true"
            aria-label="Search"
            style={{
                position: 'fixed', inset: 0, zIndex: 9999,
                background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(4px)',
                display: 'flex', alignItems: 'flex-start', justifyContent: 'center',
                paddingTop: '80px'
            }}
            onClick={onClose}
        >
            <div
                ref={panelRef}
                role="search"
                style={{
                    background: 'white', borderRadius: '14px', width: '100%', maxWidth: '560px',
                    boxShadow: '0 20px 60px rgba(0,0,0,0.3)', overflow: 'hidden'
                }}
                onClick={e => e.stopPropagation()}
            >
                {/* Search Input */}
                <div style={{ display: 'flex', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #e2e8f0' }}>
                    <span style={{ fontSize: '1.2rem', marginRight: '12px', color: '#a0aec0' }} aria-hidden="true">🔍</span>
                    <input
                        ref={inputRef}
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        onKeyDown={handleKeyDown}
                        role="combobox"
                        aria-expanded={results.length > 0}
                        aria-controls="global-search-list"
                        aria-activedescendant={activeIndex >= 0 ? `global-search-${activeIndex}` : undefined}
                        aria-label="Search pages, features, modules"
                        placeholder="Search pages, features, modules…"
                        style={{
                            flex: 1, border: 'none', outline: 'none',
                            fontSize: '1rem', background: 'transparent', color: '#2d3748'
                        }}
                    />
                    <button type="button" onClick={onClose} aria-label="Close search" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a0aec0', fontSize: '1.3rem' }}>×</button>
                </div>

                {/* Results */}
                {results.length > 0 ? (
                    <div id="global-search-list" role="listbox" aria-label="Search results" ref={listRef} style={{ maxHeight: '380px', overflowY: 'auto' }}>
                        {results.map((r, i) => (
                            <button
                                key={r.path}
                                type="button"
                                id={`global-search-${i}`}
                                role="option"
                                aria-selected={i === activeIndex}
                                onClick={() => handleSelect(r.path)}
                                onMouseEnter={() => setActiveIndex(i)}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '12px',
                                    width: '100%', padding: '12px 18px', textAlign: 'left',
                                    border: 'none', borderBottom: i < results.length - 1 ? '1px solid #f7fafc' : 'none',
                                    background: i === activeIndex ? '#f7fafc' : 'white', cursor: 'pointer', transition: 'background 0.1s'
                                }}
                            >
                                <span style={{
                                    width: '32px', height: '32px', borderRadius: '8px',
                                    background: '#ebf8ff', display: 'flex', alignItems: 'center',
                                    justifyContent: 'center', fontSize: '1rem', flexShrink: 0
                                }} aria-hidden="true">→</span>
                                <div>
                                    <div style={{ fontWeight: '600', color: '#2d3748', fontSize: '0.9rem' }}>{r.label}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#a0aec0' }}>{r.path}</div>
                                </div>
                            </button>
                        ))}
                    </div>
                ) : query.trim() ? (
                    <div style={{ padding: '28px', textAlign: 'center', color: '#a0aec0' }}>
                        <div style={{ fontSize: '1.5rem', marginBottom: '8px' }} aria-hidden="true">🔍</div>
                        No pages found for "<strong>{query}</strong>"
                    </div>
                ) : (
                    <div style={{ padding: '20px 18px' }}>
                        <div style={{ fontSize: '0.75rem', color: '#a0aec0', marginBottom: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                            Quick Access
                        </div>
                        {[
                            { label: 'Dashboard', path: '/dashboard' },
                            { label: 'Attendance', path: '/dashboard/attendance' },
                            { label: 'Grades', path: '/dashboard/grades' },
                            { label: 'Fees', path: '/dashboard/fees' },
                            { label: 'Library', path: '/dashboard/library' },
                        ].map(qa => {
                            const match = ROUTES.find(r => r.path === qa.path);
                            if (!match || !isVisible(match)) return null;
                            return (
                                <button type="button" key={qa.path} onClick={() => handleSelect(match.path)} style={{
                                    display: 'inline-block', margin: '4px', padding: '6px 14px',
                                    border: '1px solid #e2e8f0', borderRadius: '20px', background: 'white',
                                    cursor: 'pointer', fontSize: '0.85rem', color: '#4a5568'
                                }}>
                                    {qa.label}
                                </button>
                            );
                        })}
                    </div>
                )}

                {/* Footer hint */}
                <div style={{ padding: '8px 18px', borderTop: '1px solid #f0f0f0', display: 'flex', gap: '16px', fontSize: '0.75rem', color: '#5b6472' }}>
                    <span>↑↓ to navigate</span>
                    <span>↵ to open</span>
                    <span>Esc to close</span>
                </div>
            </div>
        </div>
    );
};

export default GlobalSearch;

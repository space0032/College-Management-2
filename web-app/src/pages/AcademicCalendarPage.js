import React, { useState, useEffect, useRef, useCallback } from 'react';
import { getMonthEvents, addCalendarEvent, updateCalendarEvent, deleteCalendarEvent } from '../services/calendarService';
import './AcademicCalendarPage.css';

const MONTH_NAMES = ["January","February","March","April","May","June","July","August","September","October","November","December"];
const MONTH_SHORT = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
const DAY_LABELS = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
const TYPE_ICONS = { HOLIDAY: '🏖', EXAM: '📝', DEADLINE: '⏰', EVENT: '📅' };
const EVENT_TYPES = ['HOLIDAY','EXAM','DEADLINE','EVENT'];

const AcademicCalendarPage = () => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [events, setEvents] = useState([]);
    const [selectedDate, setSelectedDate] = useState(null);
    const [activeFilter, setActiveFilter] = useState(null);
    const [viewMode, setViewMode] = useState('grid');
    const [searchQuery, setSearchQuery] = useState('');
    const [formData, setFormData] = useState({ title: '', eventType: 'EVENT', description: '' });
    const [editingEvent, setEditingEvent] = useState(null);
    const [recurCount, setRecurCount] = useState(0);
    const [animDir, setAnimDir] = useState(null);
    const addDialogRef = useRef(null);
    const detailDialogRef = useRef(null);

    const loadEvents = useCallback(async () => {
        try {
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth() + 1;
            const res = await getMonthEvents(year, month);
            setEvents(res.data || []);
        } catch (err) { console.error(err); }
    }, [currentDate]);

    useEffect(() => { loadEvents(); }, [loadEvents]);

    const animateMonth = (dir) => {
        setAnimDir(dir);
        setTimeout(() => setAnimDir(null), 300);
    };

    const handlePrevMonth = () => { animateMonth('left'); setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1)); };
    const handleNextMonth = () => { animateMonth('right'); setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1)); };
    const handleToday = () => { animateMonth('left'); setCurrentDate(new Date()); };

    const handleMonthSelect = (e) => {
        const val = parseInt(e.target.value);
        if (val >= 0 && val <= 11) {
            animateMonth(val > currentDate.getMonth() ? 'right' : 'left');
            setCurrentDate(new Date(currentDate.getFullYear(), val, 1));
        }
    };

    const handleYearChange = (delta) => {
        animateMonth(delta > 0 ? 'right' : 'left');
        setCurrentDate(new Date(currentDate.getFullYear() + delta, currentDate.getMonth(), 1));
    };

    const openAddDialog = (dateStr) => {
        setEditingEvent(null);
        setFormData({ title: '', eventType: 'EVENT', description: '' });
        setRecurCount(0);
        setSelectedDate(dateStr);
        addDialogRef.current?.showModal();
    };

    const openEditDialog = (ev, e) => {
        e.stopPropagation();
        setEditingEvent(ev);
        setFormData({ title: ev.title, eventType: ev.eventType, description: ev.description || '' });
        setRecurCount(0);
        setSelectedDate(ev.eventDate);
        detailDialogRef.current?.close();
        addDialogRef.current?.showModal();
    };

    const handleDayClick = (dayStr) => {
        const dayEvents = events.filter(ev => ev.eventDate === dayStr);
        if (dayEvents.length > 0) {
            setSelectedDate(dayStr);
            detailDialogRef.current?.showModal();
        } else {
            openAddDialog(dayStr);
        }
    };

    const handleSaveEvent = async (e) => {
        e.preventDefault();
        try {
            if (editingEvent) {
                await updateCalendarEvent(editingEvent.id, { ...formData, eventDate: selectedDate });
            } else {
                if (recurCount > 0) {
                    const base = new Date(selectedDate + 'T00:00:00');
                    for (let i = 0; i < recurCount; i++) {
                        const d = new Date(base);
                        d.setDate(d.getDate() + i * 7);
                        const ds = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
                        await addCalendarEvent({ ...formData, eventDate: ds });
                    }
                } else {
                    await addCalendarEvent({ ...formData, eventDate: selectedDate });
                }
            }
            addDialogRef.current?.close();
            setFormData({ title: '', eventType: 'EVENT', description: '' });
            setEditingEvent(null);
            setRecurCount(0);
            loadEvents();
        } catch (err) { alert(editingEvent ? 'Failed to update event' : 'Failed to add event'); }
    };

    const handleDeleteEvent = async (id) => {
        if (!window.confirm('Remove from calendar?')) return;
        try {
            await deleteCalendarEvent(id);
            detailDialogRef.current?.close();
            loadEvents();
        } catch (err) { alert('Deletion failed'); }
    };

    const month = currentDate.getMonth();
    const year = currentDate.getFullYear();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();

    const searchLower = searchQuery.toLowerCase();
    const filteredEvents = events.filter(ev => {
        const matchType = !activeFilter || ev.eventType === activeFilter;
        const matchSearch = !searchQuery || ev.title.toLowerCase().includes(searchLower) || (ev.description && ev.description.toLowerCase().includes(searchLower));
        return matchType && matchSearch;
    });

    const blanks = Array.from({ length: firstDay }, (_, i) => <div key={`blank-${i}`} className="calendar-day empty"></div>);
    const calendarDays = Array.from({ length: daysInMonth }, (_, i) => {
        const d = i + 1;
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const dayEvents = filteredEvents.filter(ev => ev.eventDate === dateStr);
        const isToday = new Date().toDateString() === new Date(dateStr).toDateString();

        const typePriority = ['HOLIDAY','EXAM','DEADLINE','EVENT'];
        let bgClass = '';
        if (dayEvents.length > 0) {
            const primary = typePriority.find(t => dayEvents.some(ev => ev.eventType === t));
            bgClass = `day-${primary.toLowerCase()}`;
        }

        return (
            <div key={d} className={`calendar-day ${isToday ? 'today' : ''} ${bgClass}`} onClick={() => handleDayClick(dateStr)}>
                <span className="day-number">{d}</span>
                <div className="day-events">
                    {dayEvents.slice(0, 2).map((ev, idx) => (
                        <div key={idx} className={`event-label event-${ev.eventType.toLowerCase()}`} title={ev.title}>
                            <span className="event-label-icon">{TYPE_ICONS[ev.eventType] || '📅'}</span>
                            <span className="event-label-text">{ev.title}</span>
                        </div>
                    ))}
                    {dayEvents.length > 2 && (
                        <span className="event-count-badge">{dayEvents.length}</span>
                    )}
                </div>
            </div>
        );
    });

    const holidayCount = filteredEvents.filter(e => e.eventType === 'HOLIDAY').length;
    const examCount = filteredEvents.filter(e => e.eventType === 'EXAM').length;
    const deadlineCount = filteredEvents.filter(e => e.eventType === 'DEADLINE').length;
    const eventCount = filteredEvents.filter(e => e.eventType === 'EVENT').length;
    const weekdayCount = Array.from({ length: daysInMonth }, (_, i) => new Date(year, month, i + 1).getDay())
        .filter(day => day !== 0 && day !== 6).length;
    const weekdayHolidayCount = filteredEvents.filter(e => {
        if (e.eventType !== 'HOLIDAY') return false;
        const day = new Date(`${e.eventDate}T00:00:00`).getDay();
        return day !== 0 && day !== 6;
    }).length;

    const handleExportCSV = () => {
        const rows = [['Date','Title','Type','Description']];
        filteredEvents.forEach(ev => {
            rows.push([ev.eventDate, `"${ev.title}"`, ev.eventType, `"${(ev.description || '').replace(/"/g, '""')}"`]);
        });
        const csv = rows.map(r => r.join(',')).join('\n');
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `calendar-${year}-${String(month+1).padStart(2,'0')}.csv`;
        a.click(); URL.revokeObjectURL(url);
    };

    const handleExportICS = () => {
        let ics = 'BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//CollegeCalendar//EN\n';
        filteredEvents.forEach(ev => {
            const dt = ev.eventDate.replace(/-/g, '');
            ics += `BEGIN:VEVENT\nDTSTART;VALUE=DATE:${dt}\nDTEND;VALUE=DATE:${dt}\nSUMMARY:${ev.title}\nDESCRIPTION:${(ev.description || '').replace(/\n/g, '\\n')}\nEND:VEVENT\n`;
        });
        ics += 'END:VCALENDAR';
        const blob = new Blob([ics], { type: 'text/calendar' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `calendar-${year}-${String(month+1).padStart(2,'0')}.ics`;
        a.click(); URL.revokeObjectURL(url);
    };

    const selectedDayEvents = selectedDate ? filteredEvents.filter(ev => ev.eventDate === selectedDate) : [];

    const renderMiniCalendar = () => {
        const miniDays = new Date(year, month + 1, 0).getDate();
        const miniFirst = new Date(year, month, 1).getDay();
        const today = new Date();
        return (
            <div className="mini-calendar">
                <div className="mini-header">
                    <span>{MONTH_SHORT[month]} {year}</span>
                </div>
                <div className="mini-grid">
                    {['S','M','T','W','T','F','S'].map((d,i) => <div key={i} className="mini-dow">{d}</div>)}
                    {Array.from({ length: miniFirst }, (_, i) => <div key={`mb-${i}`} className="mini-day empty" />)}
                    {Array.from({ length: miniDays }, (_, i) => {
                        const d = i + 1;
                        const isToday = today.getDate() === d && today.getMonth() === month && today.getFullYear() === year;
                        const ds = `${year}-${String(month+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
                        const hasEvents = filteredEvents.some(ev => ev.eventDate === ds);
                        return <div key={d} className={`mini-day ${isToday ? 'today' : ''} ${hasEvents ? 'has-events' : ''}`}>{d}</div>;
                    })}
                </div>
            </div>
        );
    };

    const renderGridView = (
        <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) 1fr', gap: '30px' }}>
            <div className="stat-card" style={{ padding: '0', overflow: 'hidden', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}>
                <div className={`calendar-grid ${animDir ? 'slide-' + animDir : ''}`} style={{ gap: '1px', background: '#e2e8f0', padding: '1px' }}>
                    {DAY_LABELS.map(d => (
                        <div key={d} className="calendar-header-day" style={{ background: '#f1f5f9', color: '#64748b', padding: '15px 0', textTransform: 'uppercase', fontSize: '0.75rem', fontWeight: 'bold' }}>{d}</div>
                    ))}
                    {blanks}
                    {calendarDays}
                </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {renderMiniCalendar()}

                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.85rem', opacity: 0.9 }}>Month Productivity</div>
                    <div style={{ fontSize: '2.4rem', fontWeight: 'bold', margin: '10px 0' }}>{Math.max(0, weekdayCount - weekdayHolidayCount - examCount)}</div>
                    <div style={{ fontSize: '0.8rem', opacity: 0.8 }}>Standard Academic Days</div>
                </div>

                <div className="stat-card">
                    <h4 style={{ margin: '0 0 15px 0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#10b981' }} />
                        Quick Legend
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                        <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Holidays</div>
                            <div style={{ fontWeight: 'bold' }}>{holidayCount}</div>
                        </div>
                        <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Exams</div>
                            <div style={{ fontWeight: 'bold' }}>{examCount}</div>
                        </div>
                        <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Events</div>
                            <div style={{ fontWeight: 'bold' }}>{eventCount}</div>
                        </div>
                        <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Deadlines</div>
                            <div style={{ fontWeight: 'bold' }}>{deadlineCount}</div>
                        </div>
                    </div>
                </div>

                <div className="stat-card" style={{ flex: 1 }}>
                    <h4 style={{ margin: '0 0 20px 0' }}>Upcoming Milestones</h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {filteredEvents.length === 0 ? (
                            <div style={{ color: '#94a3b8', textAlign: 'center', padding: '20px' }}>No events logged for this period.</div>
                        ) : (
                            filteredEvents.slice(0, 5).map(ev => (
                                <div key={ev.id} style={{ display: 'flex', gap: '15px', alignItems: 'flex-start', cursor: 'pointer' }} onClick={() => handleDayClick(ev.eventDate)}>
                                    <div style={{
                                        padding: '8px', background: '#f1f5f9', borderRadius: '8px', textAlign: 'center', minWidth: '55px',
                                        borderTop: `3px solid var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`
                                    }}>
                                        <div style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#64748b' }}>{MONTH_SHORT[new Date(ev.eventDate).getMonth()]}</div>
                                        <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{new Date(ev.eventDate).getDate()}</div>
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{ev.title}</div>
                                        {ev.description && (
                                            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '2px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                                                {ev.description}
                                            </div>
                                        )}
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '4px' }}>
                                            <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{ev.eventType}</span>
                                            <button onClick={(e) => { e.stopPropagation(); handleDeleteEvent(ev.id); }} style={{ background: 'none', border: 'none', color: '#ef4444', fontSize: '1rem', cursor: 'pointer' }}>×</button>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );

    const renderListView = (
        <div className="stat-card" style={{ padding: '0', overflow: 'hidden', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}>
            <div style={{ padding: '20px' }}>
                <h3 style={{ margin: '0 0 20px 0', fontSize: '1.1rem', color: '#1e293b' }}>Agenda View - {MONTH_NAMES[month]} {year}</h3>
                {filteredEvents.length === 0 ? (
                    <div style={{ color: '#94a3b8', textAlign: 'center', padding: '40px 20px' }}>
                        <div style={{ fontSize: '3rem', marginBottom: '10px' }}>📭</div>
                        <div>No events for this period</div>
                        {activeFilter && <div style={{ fontSize: '0.85rem', marginTop: '8px' }}>Try clearing the filter or changing the month</div>}
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {(() => {
                            const grouped = {};
                            filteredEvents.forEach(ev => {
                                if (!grouped[ev.eventDate]) grouped[ev.eventDate] = [];
                                grouped[ev.eventDate].push(ev);
                            });
                            return Object.keys(grouped).sort().map(dateStr => {
                                const dayEvents = grouped[dateStr];
                                const dateObj = new Date(dateStr + 'T00:00:00');
                                return (
                                    <div key={dateStr} style={{ marginBottom: '16px' }}>
                                        <div style={{ fontSize: '0.75rem', fontWeight: '700', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '8px', paddingBottom: '4px', borderBottom: '1px solid #e2e8f0' }}>
                                            {dateObj.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
                                            {new Date().toDateString() === dateObj.toDateString() && (
                                                <span style={{ marginLeft: '8px', padding: '2px 8px', background: 'var(--primary-color)', color: 'white', borderRadius: '10px', fontSize: '0.65rem' }}>Today</span>
                                            )}
                                        </div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                            {dayEvents.map((ev, idx) => (
                                                <div key={`${ev.id}-${idx}`} style={{ display: 'flex', gap: '12px', padding: '12px 16px', background: '#f8fafc', borderRadius: '10px', borderLeft: `4px solid var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`, transition: 'all 0.2s' }}>
                                                    <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: `var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '1.1rem', flexShrink: 0 }}>
                                                        {TYPE_ICONS[ev.eventType]}
                                                    </div>
                                                    <div style={{ flex: 1 }}>
                                                        <div style={{ fontWeight: '600', fontSize: '0.95rem', color: '#1e293b' }}>{ev.title}</div>
                                                        {ev.description && (
                                                            <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{ev.description}</div>
                                                        )}
                                                        <div style={{ fontSize: '0.7rem', color: '#94a3b8', marginTop: '4px', display: 'flex', gap: '12px', alignItems: 'center' }}>
                                                            <span style={{ padding: '2px 8px', borderRadius: '10px', background: `var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`, color: 'white', fontWeight: '600' }}>{ev.eventType}</span>
                                                            <button onClick={() => openEditDialog(ev, { stopPropagation: () => {} })} style={{ background: 'none', border: 'none', color: '#3b82f6', cursor: 'pointer', fontSize: '0.85rem', padding: '2px 8px' }}>✏️ Edit</button>
                                                            <button onClick={() => handleDeleteEvent(ev.id)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '0.85rem', padding: '2px 8px' }}>🗑 Delete</button>
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                );
                            });
                        })()}
                    </div>
                )}
            </div>
        </div>
    );

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '15px' }}>
                    <div>
                        <h1 className="page-title">📅 Academic Chronology</h1>
                        <p className="page-subtitle">Synchronized institutional schedule and milestone tracking</p>
                    </div>
                    <div style={{ display: 'flex', gap: '8px', alignItems: 'center', background: 'white', padding: '8px 15px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                        <div style={{ display: 'flex', gap: '6px', marginRight: '8px', paddingRight: '8px', borderRight: '1px solid #e2e8f0' }}>
                            <button className={`btn btn-sm ${viewMode === 'grid' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setViewMode('grid')} title="Grid View">⊞</button>
                            <button className={`btn btn-sm ${viewMode === 'list' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setViewMode('list')} title="List View">☰</button>
                        </div>
                        <div className="export-dropdown" style={{ position: 'relative', display: 'inline-block' }}>
                            <button className="btn btn-sm btn-secondary" title="Export">📥</button>
                            <div className="export-menu">
                                <button onClick={handleExportCSV}>Export CSV</button>
                                <button onClick={handleExportICS}>Export .ics</button>
                            </div>
                        </div>
                        <button className="btn btn-sm btn-secondary" onClick={handleToday} title="Go to Today">Today</button>
                        <button className="btn btn-sm btn-secondary" onClick={() => handleYearChange(-1)} title="Previous Year">◀◀</button>
                        <button className="btn btn-sm btn-secondary" onClick={handlePrevMonth}>◀</button>
                        <select className="month-select" value={month} onChange={handleMonthSelect}>
                            {MONTH_NAMES.map((name, i) => <option key={i} value={i}>{name}</option>)}
                        </select>
                        <span style={{ fontWeight: 'bold', color: '#1e293b', minWidth: '45px', textAlign: 'center' }}>{year}</span>
                        <button className="btn btn-sm btn-secondary" onClick={handleNextMonth}>▶</button>
                        <button className="btn btn-sm btn-secondary" onClick={() => handleYearChange(1)} title="Next Year">▶▶</button>
                    </div>
                </div>
            </div>

            <div style={{ marginBottom: '20px', display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
                <input
                    className="search-input"
                    type="text"
                    placeholder="🔍 Search events..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    style={{ padding: '6px 14px', borderRadius: '20px', border: '1px solid #e2e8f0', fontSize: '0.8rem', width: '200px', outline: 'none' }}
                />
                {[null, ...EVENT_TYPES].map(filter => (
                    <button
                        key={filter || 'All'}
                        onClick={() => setActiveFilter(filter)}
                        style={{
                            padding: '6px 14px',
                            borderRadius: '20px',
                            border: 'none',
                            fontSize: '0.75rem',
                            fontWeight: '600',
                            cursor: 'pointer',
                            background: activeFilter === filter ? `var(--event-${(filter || 'all').toLowerCase()}-color, #cbd5e1)` : '#e2e8f0',
                            color: activeFilter === filter ? 'white' : '#64748b',
                            transition: 'all 0.2s'
                        }}
                    >
                        {filter || 'All'}
                    </button>
                ))}
            </div>

            {viewMode === 'grid' ? renderGridView : renderListView}

            <dialog ref={detailDialogRef} className="event-detail-dialog">
                <div style={{ padding: '0' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px 24px 12px', borderBottom: '1px solid #e2e8f0' }}>
                        <div>
                            <h2 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: '#1e293b' }}>Events for {selectedDate}</h2>
                            <p style={{ color: '#64748b', fontSize: '0.9rem', margin: '5px 0 0' }}>{selectedDayEvents.length} event(s)</p>
                        </div>
                        <button onClick={() => detailDialogRef.current?.close()} style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#94a3b8' }}>×</button>
                    </div>
                    <div style={{ padding: '16px 24px', display: 'flex', flexDirection: 'column', gap: '12px', maxHeight: '400px', overflowY: 'auto' }}>
                        {selectedDayEvents.map(ev => (
                            <div key={ev.id} style={{ padding: '16px', background: '#f8fafc', borderRadius: '12px', borderLeft: `4px solid var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)` }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                        <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '0.7rem', fontWeight: 'bold', background: `var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`, color: 'white', textTransform: 'uppercase' }}>
                                            {TYPE_ICONS[ev.eventType]} {ev.eventType}
                                        </span>
                                        <span style={{ fontWeight: '600', fontSize: '0.95rem' }}>{ev.title}</span>
                                    </div>
                                    <div style={{ display: 'flex', gap: '4px' }}>
                                        <button onClick={(e) => openEditDialog(ev, e)} style={{ background: 'none', border: 'none', color: '#3b82f6', fontSize: '1.1rem', cursor: 'pointer', padding: '4px' }} title="Edit event">✏️</button>
                                        <button onClick={() => handleDeleteEvent(ev.id)} style={{ background: 'none', border: 'none', color: '#ef4444', fontSize: '1.2rem', cursor: 'pointer', padding: '4px' }} title="Delete event">×</button>
                                    </div>
                                </div>
                                {ev.description && (
                                    <div style={{ fontSize: '0.85rem', color: '#64748b', paddingLeft: '0' }}>{ev.description}</div>
                                )}
                            </div>
                        ))}
                    </div>
                    <div style={{ borderTop: '1px solid #e2e8f0', padding: '16px 24px' }}>
                        <button onClick={() => { detailDialogRef.current?.close(); openAddDialog(selectedDate); }} className="btn btn-primary" style={{ width: '100%', padding: '12px' }}>
                            + Add New Event
                        </button>
                    </div>
                </div>
            </dialog>

            <dialog ref={addDialogRef} className="event-dialog">
                <form method="dialog" onSubmit={handleSaveEvent}>
                    <header>
                        <h2>{editingEvent ? 'Edit Event' : 'Draft Event'}</h2>
                        <button type="button" onClick={() => addDialogRef.current?.close()} aria-label="Close">×</button>
                    </header>
                    <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '20px' }}>Scheduled for: <strong>{selectedDate}</strong></p>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        <div>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '500', marginBottom: '6px', color: '#374151' }}>Subject / Title *</label>
                            <input required className="form-control" type="text" value={formData.title} onChange={e => setFormData({ ...formData, title: e.target.value })} placeholder="e.g. Mid-term Physics" style={{ padding: '10px 12px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '0.95rem', width: '100%', boxSizing: 'border-box' }} />
                        </div>
                        <div>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '500', marginBottom: '6px', color: '#374151' }}>Classification *</label>
                            <select className="form-control" value={formData.eventType} onChange={e => setFormData({ ...formData, eventType: e.target.value })} style={{ padding: '10px 12px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '0.95rem', width: '100%', boxSizing: 'border-box' }}>
                                <option value="EVENT">General Event</option>
                                <option value="HOLIDAY">Holiday (Campus Closed)</option>
                                <option value="EXAM">Institutional Exam</option>
                                <option value="DEADLINE">Academic Deadline</option>
                            </select>
                        </div>
                        <div>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '500', marginBottom: '6px', color: '#374151' }}>Context / Details</label>
                            <textarea className="form-control" rows="3" value={formData.description} onChange={e => setFormData({ ...formData, description: e.target.value })} placeholder="Optional description..." style={{ padding: '10px 12px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '0.95rem', resize: 'vertical', width: '100%', boxSizing: 'border-box' }}></textarea>
                        </div>
                        {!editingEvent && (
                            <div>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '500', marginBottom: '6px', color: '#374151' }}>Repeat Weekly</label>
                                <select className="form-control" value={recurCount} onChange={e => setRecurCount(parseInt(e.target.value))} style={{ padding: '10px 12px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '0.95rem', width: '100%', boxSizing: 'border-box' }}>
                                    <option value={0}>No repeat</option>
                                    <option value={2}>Every week for 2 weeks</option>
                                    <option value={4}>Every week for 4 weeks</option>
                                    <option value={8}>Every week for 8 weeks</option>
                                    <option value={12}>Every week for 12 weeks</option>
                                </select>
                            </div>
                        )}
                    </div>
                    <div style={{ display: 'flex', gap: '12px', marginTop: '24px', justifyContent: 'flex-end' }}>
                        <button type="button" onClick={() => addDialogRef.current?.close()} style={{ padding: '12px 24px', borderRadius: '8px', border: '1px solid #e2e8f0', background: 'white', color: '#64748b', fontWeight: '600', cursor: 'pointer' }}>Discard</button>
                        <button type="submit" style={{ padding: '12px 24px', borderRadius: '8px', border: 'none', background: 'var(--primary-color)', color: 'white', fontWeight: '600', cursor: 'pointer' }}>{editingEvent ? 'Save Changes' : 'Publish to Calendar'}</button>
                    </div>
                </form>
            </dialog>
        </div>
    );
};

export default AcademicCalendarPage;

import React, { useState, useEffect } from 'react';
import { getMonthEvents, addCalendarEvent, deleteCalendarEvent } from '../services/calendarService';
import './AcademicCalendarPage.css';

const AcademicCalendarPage = () => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [events, setEvents] = useState([]);
    const [selectedDate, setSelectedDate] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState({ title: '', eventType: 'EVENT', description: '' });

    const loadEvents = React.useCallback(async () => {
        try {
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth() + 1;
            const res = await getMonthEvents(year, month);
            setEvents(res.data || []);
        } catch (err) { console.error(err); }
    }, [currentDate]);

    useEffect(() => {
        loadEvents();
    }, [loadEvents]);

    const handlePrevMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    const handleNextMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));

    const handleDayClick = (dayStr) => {
        setSelectedDate(dayStr);
        setIsModalOpen(true);
    };

    const handleSaveEvent = async (e) => {
        e.preventDefault();
        try {
            await addCalendarEvent({ ...formData, eventDate: selectedDate });
            setIsModalOpen(false);
            setFormData({ title: '', eventType: 'EVENT', description: '' });
            loadEvents();
        } catch (err) { alert('Failed to add event'); }
    };

    const handleDeleteEvent = async (id) => {
        if (!window.confirm('Remove from calendar?')) return;
        try {
            await deleteCalendarEvent(id);
            loadEvents();
        } catch (err) { alert('Deletion failed'); }
    };

    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    const month = currentDate.getMonth();
    const year = currentDate.getFullYear();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();

    const blanks = Array.from({ length: firstDay }, (_, i) => <div key={`blank-${i}`} className="calendar-day empty"></div>);
    const calendarDays = Array.from({ length: daysInMonth }, (_, i) => {
        const d = i + 1;
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const dayEvents = events.filter(ev => ev.eventDate === dateStr);
        const isToday = new Date().toDateString() === new Date(dateStr).toDateString();

        return (
            <div key={d} className={`calendar-day ${isToday ? 'today' : ''}`} onClick={() => handleDayClick(dateStr)}>
                <span className="day-number">{d}</span>
                <div className="day-events">
                    {dayEvents.map((ev, idx) => (
                        <div key={idx} className={`event-dot event-${ev.eventType.toLowerCase()}`} title={ev.title} />
                    ))}
                </div>
            </div>
        );
    });

    // Premium Analytics
    const holidayCount = events.filter(e => e.eventType === 'HOLIDAY').length;
    const examCount = events.filter(e => e.eventType === 'EXAM').length;
    const weekdayCount = Array.from({ length: daysInMonth }, (_, i) => new Date(year, month, i + 1).getDay())
        .filter(day => day !== 0 && day !== 6).length;
    const weekdayHolidayCount = events.filter(e => {
        if (e.eventType !== 'HOLIDAY') return false;
        const day = new Date(`${e.eventDate}T00:00:00`).getDay();
        return day !== 0 && day !== 6;
    }).length;

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                    <div>
                        <h1 className="page-title">📅 Academic Chronology</h1>
                        <p className="page-subtitle">Synchronized institutional schedule and milestone tracking</p>
                    </div>
                    <div style={{ display: 'flex', gap: '15px', alignItems: 'center', background: 'white', padding: '8px 15px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                        <button className="btn btn-sm btn-secondary" onClick={handlePrevMonth}>◀</button>
                        <h3 style={{ margin: 0, minWidth: '160px', textAlign: 'center', color: '#1e293b' }}>{monthNames[month]} {year}</h3>
                        <button className="btn btn-sm btn-secondary" onClick={handleNextMonth}>▶</button>
                    </div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) 1fr', gap: '30px' }}>
                {/* Main Calendar Card */}
                <div className="stat-card" style={{ padding: '0', overflow: 'hidden', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}>
                    <div className="calendar-grid" style={{ gap: '1px', background: '#e2e8f0', padding: '1px' }}>
                        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                            <div key={d} className="calendar-header-day" style={{ background: '#f1f5f9', color: '#64748b', padding: '15px 0', textTransform: 'uppercase', fontSize: '0.75rem', fontWeight: 'bold' }}>{d}</div>
                        ))}
                        {blanks}
                        {calendarDays}
                    </div>
                </div>

                {/* Sidebar Analytics */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
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
                        </div>
                    </div>

                    <div className="stat-card" style={{ flex: 1 }}>
                        <h4 style={{ margin: '0 0 20px 0' }}>Upcoming Milestones</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            {events.length === 0 ? (
                                <div style={{ color: '#94a3b8', textAlign: 'center', padding: '20px' }}>No events logged for this period.</div>
                            ) : (
                                events.slice(0, 5).map(ev => (
                                    <div key={ev.id} style={{ display: 'flex', gap: '15px', alignItems: 'flex-start' }}>
                                        <div style={{
                                            padding: '8px', background: '#f1f5f9', borderRadius: '8px', textAlign: 'center', minWidth: '55px',
                                            borderTop: `3px solid var(--event-${ev.eventType.toLowerCase()}-color, #cbd5e1)`
                                        }}>
                                            <div style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#64748b' }}>{new Date(ev.eventDate).toLocaleString('default', { month: 'short' })}</div>
                                            <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{new Date(ev.eventDate).getDate()}</div>
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{ev.title}</div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '4px' }}>
                                                <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{ev.eventType}</span>
                                                <button onClick={() => handleDeleteEvent(ev.id)} style={{ background: 'none', border: 'none', color: '#ef4444', fontSize: '1rem', cursor: 'pointer' }}>×</button>
                                            </div>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '450px', borderRadius: '20px', padding: '30px' }}>
                        <h2 style={{ marginBottom: '5px' }}>Draft Event</h2>
                        <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '25px' }}>Scheduled for: <strong>{selectedDate}</strong></p>
                        <form onSubmit={handleSaveEvent} className="form-grid">
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Subject / Title *</label>
                                <input required className="form-control" type="text" value={formData.title} onChange={e => setFormData({ ...formData, title: e.target.value })} placeholder="e.g. Mid-term Physics" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Classification *</label>
                                <select className="form-control" value={formData.eventType} onChange={e => setFormData({ ...formData, eventType: e.target.value })}>
                                    <option value="EVENT">General Event</option>
                                    <option value="HOLIDAY">Holiday (Campus Closed)</option>
                                    <option value="EXAM">Institutional Exam</option>
                                    <option value="DEADLINE">Academic Deadline</option>
                                </select>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Context / Details</label>
                                <textarea className="form-control" rows="3" value={formData.description} onChange={e => setFormData({ ...formData, description: e.target.value })} placeholder="Optional description..."></textarea>
                            </div>
                            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '12px', marginTop: '10px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1, padding: '12px' }} onClick={() => setIsModalOpen(false)}>Discard</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '12px' }}>Publish to Calendar</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AcademicCalendarPage;

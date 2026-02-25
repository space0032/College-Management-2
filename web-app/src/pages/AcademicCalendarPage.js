import React, { useState, useEffect } from 'react';
import { getMonthEvents, addCalendarEvent, deleteCalendarEvent } from '../services/calendarService';
import './AcademicCalendarPage.css';

const AcademicCalendarPage = () => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [events, setEvents] = useState([]);
    const [selectedDate, setSelectedDate] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState({ title: '', eventType: 'EVENT', description: '' });

    useEffect(() => {
        loadEvents();
        // eslint-disable-next-line
    }, [currentDate.getFullYear(), currentDate.getMonth()]);

    const loadEvents = async () => {
        try {
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth() + 1; // 1-12
            const res = await getMonthEvents(year, month);
            setEvents(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handlePrevMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    };

    const handleNextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    };

    const handleDayClick = (dayStr) => {
        setSelectedDate(dayStr);
        setIsModalOpen(true);
    };

    const handleSaveEvent = async (e) => {
        e.preventDefault();
        if (!selectedDate) return;

        try {
            await addCalendarEvent({
                ...formData,
                eventDate: selectedDate
            });
            setIsModalOpen(false);
            setFormData({ title: '', eventType: 'EVENT', description: '' });
            loadEvents();
        } catch (err) {
            alert('Failed to add event');
        }
    };

    const handleDeleteEvent = async (id) => {
        if (!window.confirm('Delete this event?')) return;
        try {
            await deleteCalendarEvent(id);
            loadEvents();
        } catch (err) {
            alert('Failed to delete event');
        }
    };

    // Calendar rendering logic
    const getDaysInMonth = (year, month) => new Date(year, month + 1, 0).getDate();
    const getFirstDayOfMonth = (year, month) => new Date(year, month, 1).getDay();

    const daysInMonth = getDaysInMonth(currentDate.getFullYear(), currentDate.getMonth());
    const firstDay = getFirstDayOfMonth(currentDate.getFullYear(), currentDate.getMonth());

    const blanks = Array.from({ length: firstDay }, (_, i) => <div key={`blank-${i}`} className="calendar-day empty"></div>);

    const days = Array.from({ length: daysInMonth }, (_, i) => {
        const dayNum = i + 1;
        const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;

        // Find events for this day
        const dayEvents = events.filter(e => e.eventDate === dateStr);

        const isToday = new Date().toDateString() === new Date(dateStr).toDateString();

        return (
            <div key={`day-${dayNum}`} className={`calendar-day ${isToday ? 'today' : ''}`} onClick={() => handleDayClick(dateStr)}>
                <span className="day-number">{dayNum}</span>
                <div className="day-events">
                    {dayEvents.map((ev, idx) => (
                        <div key={idx} className={`event-badge event-${ev.eventType.toLowerCase()}`} title={ev.description}>
                            {ev.title}
                        </div>
                    ))}
                </div>
            </div>
        );
    });

    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

    return (
        <div className="page-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2>Academic Calendar</h2>
                <div className="calendar-controls">
                    <button className="btn btn-secondary" onClick={handlePrevMonth}>&lt; Prev</button>
                    <h3 style={{ margin: '0 20px', minWidth: '200px', textAlign: 'center' }}>
                        {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
                    </h3>
                    <button className="btn btn-secondary" onClick={handleNextMonth}>Next &gt;</button>
                </div>
            </div>

            <div className="stat-card" style={{ padding: 0, overflow: 'hidden' }}>
                <div className="calendar-grid">
                    {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                        <div key={d} className="calendar-header-day">{d}</div>
                    ))}
                    {blanks}
                    {days}
                </div>
            </div>

            {/* Upcoming/Legend Sidebar Area */}
            <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
                <div className="stat-card" style={{ flex: 1 }}>
                    <h3>Event Types</h3>
                    <ul style={{ listStyle: 'none', padding: 0, marginTop: '15px' }}>
                        <li style={{ marginBottom: '10px' }}><span className="badge event-holiday">HOLIDAY</span> Institutional Holidays</li>
                        <li style={{ marginBottom: '10px' }}><span className="badge event-exam">EXAM</span> Academic Examinations</li>
                        <li style={{ marginBottom: '10px' }}><span className="badge event-deadline">DEADLINE</span> Project/Fee Deadlines</li>
                        <li style={{ marginBottom: '10px' }}><span className="badge event-event">EVENT</span> Extracurricular & System Events</li>
                    </ul>
                </div>

                <div className="stat-card" style={{ flex: 2 }}>
                    <h3>Manage This Month's Events</h3>
                    <div className="data-table-container mt-2">
                        <table className="data-table">
                            <thead><tr><th>Date</th><th>Type</th><th>Title</th><th>Action</th></tr></thead>
                            <tbody>
                                {events.length === 0 ? <tr><td colSpan="4" style={{ textAlign: 'center' }}>No events this month</td></tr> :
                                    events.map(ev => (
                                        <tr key={ev.id}>
                                            <td>{ev.eventDate}</td>
                                            <td><span className={`badge event-${ev.eventType.toLowerCase()}`}>{ev.eventType}</span></td>
                                            <td>{ev.title}</td>
                                            <td>
                                                <button className="btn btn-danger btn-sm" onClick={() => handleDeleteEvent(ev.id)}>Delete</button>
                                            </td>
                                        </tr>
                                    ))
                                }
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Add Event Modal */}
            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '400px' }}>
                        <h2>Add Event on {selectedDate}</h2>
                        <form onSubmit={handleSaveEvent} style={{ marginTop: '20px' }}>
                            <div className="form-group">
                                <label>Event Title *</label>
                                <input required type="text" value={formData.title} onChange={e => setFormData({ ...formData, title: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Event Type *</label>
                                <select value={formData.eventType} onChange={e => setFormData({ ...formData, eventType: e.target.value })}>
                                    <option value="EVENT">System Event</option>
                                    <option value="HOLIDAY">Holiday</option>
                                    <option value="EXAM">Examination</option>
                                    <option value="DEADLINE">Deadline</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Description</label>
                                <textarea rows="3" value={formData.description} onChange={e => setFormData({ ...formData, description: e.target.value })}></textarea>
                            </div>
                            <div className="form-actions" style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                                <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary">Create Event</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

        </div>
    );
};

export default AcademicCalendarPage;

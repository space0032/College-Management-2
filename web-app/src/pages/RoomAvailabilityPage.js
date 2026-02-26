import React, { useState, useEffect } from 'react';
import { getRooms, checkAvailability } from '../services/roomService';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const TIME_SLOTS = [
    '09:00 - 10:00 AM', '10:00 - 11:00 AM', '11:00 - 12:00 PM',
    '12:00 - 01:00 PM', '01:00 - 02:00 PM', '02:00 - 03:00 PM',
    '03:00 - 04:00 PM', '04:00 - 05:00 PM'
];
const ROOM_TYPES = ['All', 'CLASSROOM', 'LAB', 'SEMINAR', 'AUDITORIUM', 'OFFICE'];

const getTodayDay = () => {
    const jsDay = new Date().getDay(); // 0=Sun, 1=Mon,...
    const map = { 1: 'Monday', 2: 'Tuesday', 3: 'Wednesday', 4: 'Thursday', 5: 'Friday', 6: 'Saturday', 0: 'Monday' };
    return map[jsDay] || 'Monday';
};

const getCurrentSlot = () => {
    const h = new Date().getHours();
    if (h < 9) return TIME_SLOTS[0];
    if (h >= 17) return TIME_SLOTS[TIME_SLOTS.length - 1];
    const idx = Math.min(h - 9, TIME_SLOTS.length - 1);
    return TIME_SLOTS[idx];
};

const RoomAvailabilityPage = () => {
    const [allRooms, setAllRooms] = useState([]);
    const [availability, setAvailability] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [typeFilter, setTypeFilter] = useState('All');
    const [hasQueried, setHasQueried] = useState(false);

    const [searchParams, setSearchParams] = useState({
        day: getTodayDay(),
        timeSlot: getCurrentSlot()
    });

    useEffect(() => {
        getRooms().then(res => {
            setAllRooms(Array.isArray(res.data) ? res.data : []);
        }).catch(() => { });
        // Auto-check today
        const runInit = async () => {
            setLoading(true);
            try {
                const res = await checkAvailability(getTodayDay(), getCurrentSlot());
                setAvailability(Array.isArray(res.data) ? res.data : []);
                setHasQueried(true);
            } catch {
                setError('Failed to query room availability.');
            } finally {
                setLoading(false);
            }
        };
        runInit();
    }, []);

    const handleCheck = async (e, overrideDay, overrideSlot) => {
        if (e) e.preventDefault();
        setLoading(true);
        setError(null);
        const day = overrideDay || searchParams.day;
        const timeSlot = overrideSlot || searchParams.timeSlot;
        try {
            const res = await checkAvailability(day, timeSlot);
            setAvailability(Array.isArray(res.data) ? res.data : []);
            setHasQueried(true);
        } catch {
            setError('Failed to query room availability.');
        } finally {
            setLoading(false);
        }
    };

    const handleTodayClick = () => {
        const today = getTodayDay();
        const slot = getCurrentSlot();
        setSearchParams({ day: today, timeSlot: slot });
        handleCheck(null, today, slot);
    };

    const filtered = typeFilter === 'All'
        ? availability
        : availability.filter(r => (r.type || '').toUpperCase() === typeFilter);

    const available = filtered.filter(r => r.isAvailable);
    const occupied = filtered.filter(r => !r.isAvailable);

    // Stats from all rooms
    const totalAvailable = availability.filter(r => r.isAvailable).length;
    const totalOccupied = availability.filter(r => !r.isAvailable).length;
    const totalRooms = availability.length || allRooms.length;

    const RoomCard = ({ room, available: av }) => (
        <div style={{
            background: 'white',
            border: `1px solid ${av ? '#9ae6b4' : '#feb2b2'}`,
            borderLeft: `5px solid ${av ? '#38a169' : '#e53e3e'}`,
            borderRadius: '10px', padding: '16px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.06)'
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <div style={{ fontSize: '1.3rem', fontWeight: 'bold', color: '#2d3748' }}>
                    {room.roomNumber || room.name}
                </div>
                <span style={{
                    background: av ? '#f0fff4' : '#fff5f5',
                    color: av ? '#276749' : '#c53030',
                    padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600'
                }}>
                    {av ? '✓ Available' : '✗ Occupied'}
                </span>
            </div>
            <div style={{ display: 'flex', gap: '12px', fontSize: '0.82rem', color: '#718096' }}>
                {room.type && (
                    <span style={{ background: '#ebf8ff', color: '#2b6cb0', padding: '2px 8px', borderRadius: '10px' }}>
                        {room.type}
                    </span>
                )}
                {room.capacity && <span>👥 {room.capacity}</span>}
                {room.building && <span>🏢 {room.building}</span>}
            </div>
            {!av && room.course && (
                <div style={{ marginTop: '8px', fontSize: '0.8rem', color: '#718096' }}>
                    📚 {room.course}
                </div>
            )}
        </div>
    );

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>🚪 Room Availability</h2>
                    <p className="text-muted">Check real-time classroom and lab availability by day and time slot.</p>
                </div>
                <button className="btn btn-secondary" onClick={handleTodayClick} title="Jump to today's schedule">
                    📅 Today's Schedule
                </button>
            </div>

            {/* Search Form */}
            <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '18px', marginBottom: '24px' }}>
                <form onSubmit={handleCheck} style={{ display: 'flex', gap: '14px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
                    <div className="form-group" style={{ margin: 0, flex: 1, minWidth: '160px' }}>
                        <label>Day</label>
                        <select value={searchParams.day} onChange={e => setSearchParams(p => ({ ...p, day: e.target.value }))}>
                            {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                    </div>
                    <div className="form-group" style={{ margin: 0, flex: 1, minWidth: '200px' }}>
                        <label>Time Slot</label>
                        <select value={searchParams.timeSlot} onChange={e => setSearchParams(p => ({ ...p, timeSlot: e.target.value }))}>
                            {TIME_SLOTS.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </div>
                    <div className="form-group" style={{ margin: 0, flex: 1, minWidth: '160px' }}>
                        <label>Room Type</label>
                        <select value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
                            {ROOM_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </div>
                    <button type="submit" className="btn btn-primary" style={{ height: '42px', minWidth: '140px' }}>
                        {loading ? 'Checking…' : 'Check Availability'}
                    </button>
                </form>
            </div>

            {error && <div style={{ color: '#e53e3e', marginBottom: '16px', padding: '12px', background: '#fff5f5', borderRadius: '8px' }}>{error}</div>}

            {/* Stats Row */}
            {hasQueried && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '14px', marginBottom: '24px' }}>
                    <div style={{ padding: '16px', background: '#ebf8ff', borderRadius: '10px', border: '1px solid #bee3f8', textAlign: 'center' }}>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#2b6cb0' }}>{totalRooms}</div>
                        <div style={{ fontSize: '0.82rem', color: '#4a5568' }}>Total Rooms</div>
                    </div>
                    <div style={{ padding: '16px', background: '#f0fff4', borderRadius: '10px', border: '1px solid #9ae6b4', textAlign: 'center' }}>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#276749' }}>{totalAvailable}</div>
                        <div style={{ fontSize: '0.82rem', color: '#4a5568' }}>Available</div>
                    </div>
                    <div style={{ padding: '16px', background: '#fff5f5', borderRadius: '10px', border: '1px solid #feb2b2', textAlign: 'center' }}>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#c53030' }}>{totalOccupied}</div>
                        <div style={{ fontSize: '0.82rem', color: '#4a5568' }}>Occupied</div>
                    </div>
                </div>
            )}

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Checking availability...</div>
            ) : hasQueried ? (
                <>
                    {available.length > 0 && (
                        <>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '14px' }}>
                                <h3 style={{ margin: 0, color: '#276749' }}>✓ Available Rooms</h3>
                                <span style={{ background: '#f0fff4', color: '#276749', padding: '3px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600' }}>{available.length}</span>
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '14px', marginBottom: '28px' }}>
                                {available.map(r => <RoomCard key={r.roomNumber || r.id} room={r} available={true} />)}
                            </div>
                        </>
                    )}
                    {occupied.length > 0 && (
                        <>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '14px' }}>
                                <h3 style={{ margin: 0, color: '#c53030' }}>✗ Occupied Rooms</h3>
                                <span style={{ background: '#fff5f5', color: '#c53030', padding: '3px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600' }}>{occupied.length}</span>
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '14px' }}>
                                {occupied.map(r => <RoomCard key={r.roomNumber || r.id} room={r} available={false} />)}
                            </div>
                        </>
                    )}
                    {filtered.length === 0 && (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                            No rooms match the selected filter.
                        </div>
                    )}
                </>
            ) : (
                <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '12px' }}>🚪</div>
                    Select a day and time slot to view room availability.
                </div>
            )}
        </div>
    );
};

export default RoomAvailabilityPage;

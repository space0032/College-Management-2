import React, { useState, useEffect } from 'react';
import { getRooms, checkAvailability, getFreeSlots, getDayGrid, createRoom, deleteRoom } from '../services/roomService';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
// Canonical 24h slots — must match backend TimeSlotUtil.CANONICAL_SLOTS.
// Backend parses legacy "09:00 - 10:00 AM" data via overlap, so old rows still block correctly.
const TIME_SLOTS = [
    '09:00 - 10:00', '10:00 - 11:00', '11:00 - 12:00',
    '12:00 - 13:00', '13:00 - 14:00', '14:00 - 15:00',
    '15:00 - 16:00', '16:00 - 17:00'
];
const ROOM_TYPES = ['All', 'CLASSROOM', 'LABORATORY', 'SEMINAR', 'AUDITORIUM', 'OFFICE'];

const getTodayDay = () => {
    const jsDay = new Date().getDay(); // 0=Sun, 1=Mon,...
    const map = { 1: 'Monday', 2: 'Tuesday', 3: 'Wednesday', 4: 'Thursday', 5: 'Friday', 6: 'Saturday', 0: 'Sunday' };
    return map[jsDay] || 'Monday';
};

const getCurrentSlot = () => {
    const h = new Date().getHours();
    if (h < 9) return TIME_SLOTS[0];
    if (h >= 17) return TIME_SLOTS[TIME_SLOTS.length - 1];
    const idx = Math.min(Math.max(h - 9, 0), TIME_SLOTS.length - 1);
    return TIME_SLOTS[idx];
};

const RoomAvailabilityPage = () => {
    const [allRooms, setAllRooms] = useState([]);
    const [availability, setAvailability] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [typeFilter, setTypeFilter] = useState('All');
    const [minCapacity, setMinCapacity] = useState('');
    const [buildingFilter, setBuildingFilter] = useState('');
    const [hasQueried, setHasQueried] = useState(false);
    const [view, setView] = useState('check'); // check | grid | free | manage
    const [grid, setGrid] = useState(null);
    const [freeResult, setFreeResult] = useState(null);
    const [freeRoom, setFreeRoom] = useState('');
    const [newRoom, setNewRoom] = useState({ roomNumber: '', building: '', capacity: 40, type: 'CLASSROOM' });

    const [searchParams, setSearchParams] = useState({
        day: getTodayDay(),
        timeSlot: getCurrentSlot()
    });

    const filters = {
        type: typeFilter,
        minCapacity: minCapacity === '' ? undefined : Number(minCapacity),
        building: buildingFilter || undefined
    };

    const loadInventory = () => {
        getRooms().then(res => {
            const list = Array.isArray(res.data) ? res.data : [];
            setAllRooms(list);
            if (!freeRoom && list.length > 0) setFreeRoom(list[0].roomNumber);
        }).catch(err => setError(err.response?.data?.error || 'Room inventory could not be loaded.'));
    };

    useEffect(() => {
        loadInventory();
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
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleCheck = async (e, overrideDay, overrideSlot) => {
        if (e) e.preventDefault();
        setLoading(true);
        setError(null);
        const day = overrideDay || searchParams.day;
        const timeSlot = overrideSlot || searchParams.timeSlot;
        try {
            const res = await checkAvailability(day, timeSlot, filters);
            setAvailability(Array.isArray(res.data) ? res.data : []);
            setHasQueried(true);
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to query room availability.');
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

    const handleGrid = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await getDayGrid(searchParams.day, { type: typeFilter, building: buildingFilter });
            setGrid(res.data);
            setView('grid');
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to load day grid.');
        } finally {
            setLoading(false);
        }
    };

    const handleFree = async (e) => {
        if (e) e.preventDefault();
        if (!freeRoom) {
            setError('Select a room first.');
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const res = await getFreeSlots(searchParams.day, freeRoom);
            setFreeResult(res.data);
            setView('free');
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to load free slots.');
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async (e) => {
        if (e) e.preventDefault();
        if (!newRoom.roomNumber.trim()) {
            setError('Room number is required.');
            return;
        }
        setLoading(true);
        setError(null);
        try {
            await createRoom({ ...newRoom, capacity: Number(newRoom.capacity) || 40 });
            setNewRoom({ roomNumber: '', building: '', capacity: 40, type: 'CLASSROOM' });
            loadInventory();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to create room.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this room?')) return;
        try {
            await deleteRoom(id);
            loadInventory();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to delete room.');
        }
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
            <div style={{ display: 'flex', gap: '12px', fontSize: '0.82rem', color: '#718096', flexWrap: 'wrap' }}>
                {room.type && (
                    <span style={{ background: '#ebf8ff', color: '#2b6cb0', padding: '2px 8px', borderRadius: '10px' }}>
                        {room.type}
                    </span>
                )}
                {room.capacity != null && <span>👥 {room.capacity}</span>}
                {room.building && <span>🏢 {room.building}</span>}
            </div>
            {!av && (room.occupiedBy || room.course) && (
                <div style={{ marginTop: '8px', fontSize: '0.8rem', color: '#4a5568', background: '#fff5f5', padding: '8px', borderRadius: '6px' }}>
                    📚 {room.occupiedBy
                        ? `${room.occupiedBy.subject} — ${room.occupiedBy.facultyName || 'TBA'} (${room.occupiedBy.department || ''}${room.occupiedBy.semester ? ` Sem ${room.occupiedBy.semester}` : ''})`
                        : room.course}
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

            {/* View tabs */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
                {[
                    ['check', '🔍 Check'],
                    ['grid', '🗓️ Day Grid'],
                    ['free', '🕒 Free Slots'],
                    ['manage', '⚙️ Manage Rooms']
                ].map(([key, label]) => (
                    <button key={key} className={`btn ${view === key ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => { setView(key); if (key === 'grid') handleGrid(); }}>
                        {label}
                    </button>
                ))}
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
                    <div className="form-group" style={{ margin: 0, flex: 1, minWidth: '140px' }}>
                        <label>Room Type</label>
                        <select value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
                            {ROOM_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </div>
                    <div className="form-group" style={{ margin: 0, width: '120px' }}>
                        <label>Min Cap.</label>
                        <input type="number" min="0" placeholder="Any" value={minCapacity}
                            onChange={e => setMinCapacity(e.target.value)} />
                    </div>
                    <div className="form-group" style={{ margin: 0, flex: 1, minWidth: '140px' }}>
                        <label>Building</label>
                        <input type="text" placeholder="Any building" value={buildingFilter}
                            onChange={e => setBuildingFilter(e.target.value)} />
                    </div>
                    <button type="submit" className="btn btn-primary" style={{ height: '42px', minWidth: '140px' }}>
                        {loading ? 'Checking…' : 'Check Availability'}
                    </button>
                </form>
            </div>

            {error && <div style={{ color: '#e53e3e', marginBottom: '16px', padding: '12px', background: '#fff5f5', borderRadius: '8px' }}>{error}</div>}

            {view === 'check' && (
                <>
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
                </>
            )}

            {view === 'grid' && grid && (
                <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px', overflowX: 'auto' }}>
                    <h3 style={{ marginTop: 0 }}>🗓️ {grid.day} — Rooms × Slots</h3>
                    <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: '0.8rem' }}>
                        <thead>
                            <tr>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}>Room</th>
                                {grid.slots.map(s => <th key={s} style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc', whiteSpace: 'nowrap' }}>{s}</th>)}
                            </tr>
                        </thead>
                        <tbody>
                            {grid.rooms.map(r => (
                                <tr key={r.roomNumber}>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px', fontWeight: '600' }}>
                                        {r.roomNumber}<br />
                                        <span style={{ fontWeight: '400', color: '#718096' }}>{r.type}{r.capacity ? ` · ${r.capacity}` : ''}</span>
                                    </td>
                                    {grid.slots.map(s => {
                                        const cell = r.slots[s];
                                        return (
                                            <td key={s} style={{
                                                border: '1px solid #e2e8f0', padding: '8px',
                                                background: cell ? '#fff5f5' : '#f0fff4',
                                                color: cell ? '#c53030' : '#276749'
                                            }} title={cell ? `${cell.subject} — ${cell.facultyName || ''}` : 'Free'}>
                                                {cell ? `✗ ${cell.subject}` : '✓'}
                                            </td>
                                        );
                                    })}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {grid.rooms.length === 0 && <div style={{ padding: '24px', textAlign: 'center', color: '#888' }}>No rooms in inventory.</div>}
                </div>
            )}

            {view === 'free' && (
                <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '18px' }}>
                    <h3 style={{ marginTop: 0 }}>🕒 Find free slots</h3>
                    <form onSubmit={handleFree} style={{ display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: '16px' }}>
                        <div className="form-group" style={{ margin: 0, minWidth: '200px' }}>
                            <label>Room</label>
                            <select value={freeRoom} onChange={e => setFreeRoom(e.target.value)}>
                                {allRooms.map(r => <option key={r.id || r.roomNumber} value={r.roomNumber}>{r.roomNumber}</option>)}
                            </select>
                        </div>
                        <button type="submit" className="btn btn-primary">{loading ? 'Loading…' : 'Find'}</button>
                    </form>
                    {freeResult && (
                        <div>
                            <p className="text-muted">{freeResult.roomNumber} on {freeResult.day}: {freeResult.freeSlots.length} free slot(s)</p>
                            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                {freeResult.freeSlots.map(s => (
                                    <span key={s} style={{ background: '#f0fff4', color: '#276749', padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', border: '1px solid #9ae6b4' }}>✓ {s}</span>
                                ))}
                                {freeResult.freeSlots.length === 0 && <span style={{ color: '#888' }}>Fully booked that day.</span>}
                            </div>
                        </div>
                    )}
                </div>
            )}

            {view === 'manage' && (
                <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '18px' }}>
                    <h3 style={{ marginTop: 0 }}>⚙️ Manage rooms</h3>
                    <form onSubmit={handleCreate} style={{ display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: '20px' }}>
                        <div className="form-group" style={{ margin: 0 }}>
                            <label>Room No *</label>
                            <input type="text" value={newRoom.roomNumber} onChange={e => setNewRoom({ ...newRoom, roomNumber: e.target.value })} placeholder="e.g. A-101" />
                        </div>
                        <div className="form-group" style={{ margin: 0 }}>
                            <label>Building</label>
                            <input type="text" value={newRoom.building} onChange={e => setNewRoom({ ...newRoom, building: e.target.value })} placeholder="Main Block" />
                        </div>
                        <div className="form-group" style={{ margin: 0, width: '110px' }}>
                            <label>Capacity</label>
                            <input type="number" min="1" value={newRoom.capacity} onChange={e => setNewRoom({ ...newRoom, capacity: e.target.value })} />
                        </div>
                        <div className="form-group" style={{ margin: 0 }}>
                            <label>Type</label>
                            <select value={newRoom.type} onChange={e => setNewRoom({ ...newRoom, type: e.target.value })}>
                                {ROOM_TYPES.filter(t => t !== 'All').map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </div>
                        <button type="submit" className="btn btn-primary">Add Room</button>
                    </form>
                    <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: '0.85rem' }}>
                        <thead>
                            <tr>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}>Room</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}>Building</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}>Capacity</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}>Type</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: '8px', background: '#f7fafc' }}></th>
                            </tr>
                        </thead>
                        <tbody>
                            {allRooms.map(r => (
                                <tr key={r.id || r.roomNumber}>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px', fontWeight: '600' }}>{r.roomNumber}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px' }}>{r.building || '—'}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px' }}>{r.capacity || '—'}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px' }}>{r.type || '—'}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: '8px' }}>
                                        {r.id && <button className="btn btn-secondary" onClick={() => handleDelete(r.id)}>Delete</button>}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {allRooms.length === 0 && <div style={{ padding: '24px', textAlign: 'center', color: '#888' }}>No rooms yet — add the first one above.</div>}
                </div>
            )}
        </div>
    );
};

export default RoomAvailabilityPage;

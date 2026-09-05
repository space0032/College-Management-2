import React, { useState, useEffect, useRef } from 'react';
import './RoomAvailabilityPage.css';
import SessionManager from '../utils/SessionManager';
import BookRoomModal from './BookRoomModal';
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
    const [booking, setBooking] = useState(null); // { roomNumber, day, timeSlot, source }
    const [notice, setNotice] = useState(null);
    // Guards against stale async responses overwriting the latest tab selection.
    const requestSeq = useRef(0);

    const canBook = SessionManager.hasPermission('BOOK_ROOM') || SessionManager.hasPermission('CREATE_TIMETABLE');

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
            setFreeRoom(prev => prev || (list.length > 0 ? list[0].roomNumber : ''));
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
        requestSeq.current++;
        setLoading(true);
        setError(null);
        setNotice(null);
        const day = overrideDay || searchParams.day;
        const timeSlot = overrideSlot || searchParams.timeSlot;
        try {
            const res = await checkAvailability(day, timeSlot, filters);
            setAvailability(Array.isArray(res.data) ? res.data : []);
            setHasQueried(true);
            setView('check');
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
        const seq = ++requestSeq.current;
        setLoading(true);
        setError(null);
        setNotice(null);
        setView('grid');
        try {
            const res = await getDayGrid(searchParams.day, { type: typeFilter, building: buildingFilter });
            if (requestSeq.current !== seq) return;
            setGrid(res.data);
        } catch (err) {
            if (requestSeq.current !== seq) return;
            setError(err.response?.data?.error || 'Failed to load day grid.');
        } finally {
            if (requestSeq.current === seq) setLoading(false);
        }
    };

    const handleFree = async (e) => {
        if (e) e.preventDefault();
        if (!freeRoom) {
            setError('Select a room first.');
            return;
        }
        const seq = ++requestSeq.current;
        setLoading(true);
        setError(null);
        setNotice(null);
        setView('free');
        try {
            const res = await getFreeSlots(searchParams.day, freeRoom);
            if (requestSeq.current !== seq) return;
            setFreeResult(res.data);
        } catch (err) {
            if (requestSeq.current !== seq) return;
            setError(err.response?.data?.error || 'Failed to load free slots.');
        } finally {
            if (requestSeq.current === seq) setLoading(false);
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

    const openBooking = (roomNumber, day, timeSlot, source) => {
        setNotice(null);
        setError(null);
        setBooking({ roomNumber, day, timeSlot, source });
    };

    const handleBooked = async () => {
        if (!booking) return;
        const done = { ...booking };
        setBooking(null);
        const msg = `✅ ${done.roomNumber} booked for ${done.day} · ${done.timeSlot}.`;
        if (done.source === 'grid') {
            await handleGrid();
        } else if (done.source === 'free') {
            setLoading(true);
            try {
                const res = await getFreeSlots(done.day, done.roomNumber);
                setFreeResult(res.data);
            } catch (err) {
                setError(err.response?.data?.error || 'Booking saved, but free slots could not be refreshed.');
            } finally {
                setLoading(false);
            }
        } else {
            await handleCheckAfterBooking(done);
        }
        setNotice(msg);
    };

    const handleCheckAfterBooking = async (done) => {
        setLoading(true);
        try {
            const res = await checkAvailability(done.day, done.timeSlot, filters);
            setAvailability(Array.isArray(res.data) ? res.data : []);
            setHasQueried(true);
            setView('check');
        } catch (err) {
            setError(err.response?.data?.error || 'Booking saved, but availability could not be refreshed.');
        } finally {
            setLoading(false);
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
        <div className={`room-card ${av ? 'available' : 'occupied'}`}>
            <div className="room-card-top">
                <div className="room-card-name">{room.roomNumber || room.name}</div>
                <span className={`badge ${av ? 'badge-success' : 'badge-danger'}`}>
                    {av ? '✓ Available' : '✗ Occupied'}
                </span>
            </div>
            <div className="room-card-meta">
                {room.type && <span className="badge badge-primary">{room.type}</span>}
                {room.capacity != null && <span>👥 {room.capacity}</span>}
                {room.building && <span>🏢 {room.building}</span>}
            </div>
            {!av && (room.occupiedBy || room.course) && (
                <div className="room-occupant">
                    {room.occupiedBy ? (
                        <>
                            <strong>📚 {room.occupiedBy.subject}</strong>
                            <span>
                                {room.occupiedBy.facultyName || 'TBA'}
                                {room.occupiedBy.department ? ` · ${room.occupiedBy.department}` : ''}
                                {room.occupiedBy.semester ? ` · Sem ${room.occupiedBy.semester}` : ''}
                            </span>
                        </>
                    ) : (
                        <>📚 {room.course}</>
                    )}
                </div>
            )}
            {av && canBook && (
                <button
                    className="btn btn-primary btn-sm room-card-book"
                    onClick={() => openBooking(room.roomNumber, searchParams.day, searchParams.timeSlot, 'check')}
                >
                    📅 Book this slot
                </button>
            )}
        </div>
    );

    const TABS = [
        ['check', '🔍 Check'],
        ['grid', '🗓️ Day Grid'],
        ['free', '🕒 Free Slots'],
        ['manage', '⚙️ Manage Rooms']
    ];

    const handleTab = (key) => {
        if (key === 'grid') {
            handleGrid();
        } else if (key === 'free') {
            // Switch immediately so a pending grid load cannot revert the tab.
            // Bump the sequence to invalidate stale loads and clear any
            // in-flight spinner — the stale finally-block must not leave
            // the new view disabled (D08).
            requestSeq.current++;
            setLoading(false);
            setError(null);
            setView('free');
        } else {
            requestSeq.current++;
            setLoading(false);
            setView(key);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🚪 Room Availability</h1>
                    <p className="text-muted">Check real-time classroom and lab availability by day and time slot.</p>
                </div>
                <div className="page-actions">
                    <button className="btn btn-secondary" onClick={handleTodayClick} title="Jump to today's schedule">
                        📅 Today's Schedule
                    </button>
                </div>
            </div>

            {/* View tabs */}
            <div className="tab-buttons" role="tablist" aria-label="Room availability views">
                {TABS.map(([key, label]) => (
                    <button key={key} role="tab" aria-selected={view === key}
                        className={`btn btn-tab ${view === key ? 'active' : ''}`}
                        onClick={() => handleTab(key)}>
                        {label}
                    </button>
                ))}
            </div>

            {/* Search Form */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="card-body">
                    <form onSubmit={handleCheck} className="room-filter-grid">
                        <div className="form-group">
                            <label className="form-label" htmlFor="room-day">Day</label>
                            <select id="room-day" className="form-control" value={searchParams.day} onChange={e => setSearchParams(p => ({ ...p, day: e.target.value }))}>
                                {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="room-slot">Time Slot</label>
                            <select id="room-slot" className="form-control" value={searchParams.timeSlot} onChange={e => setSearchParams(p => ({ ...p, timeSlot: e.target.value }))}>
                                {TIME_SLOTS.map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="room-type">Room Type</label>
                            <select id="room-type" className="form-control" value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
                                {ROOM_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="room-cap">Min Capacity</label>
                            <input id="room-cap" className="form-control" type="number" min="0" placeholder="Any" value={minCapacity}
                                onChange={e => setMinCapacity(e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="room-building">Building</label>
                            <input id="room-building" className="form-control" type="text" placeholder="Any building" value={buildingFilter}
                                onChange={e => setBuildingFilter(e.target.value)} />
                        </div>
                        <div className="room-filter-actions">
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? 'Checking…' : 'Check Availability'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {error && <div className="alert alert-error" style={{ marginBottom: '16px' }}>{error}</div>}
            {notice && <div className="alert alert-success" style={{ marginBottom: '16px' }}>{notice}</div>}

            {view === 'check' && (
                <>
                    {/* Stats Row */}
                    {hasQueried && (
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-card-icon">🚪</div>
                                <div className="stat-card-value">{totalRooms}</div>
                                <div className="stat-card-label">Total Rooms</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-card-icon">✅</div>
                                <div className="stat-card-value">{totalAvailable}</div>
                                <div className="stat-card-label">Available</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-card-icon">⛔</div>
                                <div className="stat-card-value">{totalOccupied}</div>
                                <div className="stat-card-label">Occupied</div>
                            </div>
                        </div>
                    )}

                    {loading ? (
                        <div className="loading-container">
                            <div className="spinner" />
                            <div>Checking availability...</div>
                        </div>
                    ) : hasQueried ? (
                        <>
                            {available.length > 0 && (
                                <>
                                    <div className="room-section-head available">
                                        <h3>✓ Available Rooms</h3>
                                        <span className="badge badge-success">{available.length}</span>
                                    </div>
                                    <div className="room-grid">
                                        {available.map(r => <RoomCard key={r.roomNumber || r.id} room={r} available={true} />)}
                                    </div>
                                </>
                            )}
                            {occupied.length > 0 && (
                                <>
                                    <div className="room-section-head occupied">
                                        <h3>✗ Occupied Rooms</h3>
                                        <span className="badge badge-danger">{occupied.length}</span>
                                    </div>
                                    <div className="room-grid">
                                        {occupied.map(r => <RoomCard key={r.roomNumber || r.id} room={r} available={false} />)}
                                    </div>
                                </>
                            )}
                            {filtered.length === 0 && (
                                <div className="table-empty">
                                    <div className="table-empty-icon">🔍</div>
                                    <div>No rooms match the selected filter.</div>
                                </div>
                            )}
                        </>
                    ) : (
                        <div className="table-empty">
                            <div className="table-empty-icon">🚪</div>
                            <div>Select a day and time slot to view room availability.</div>
                        </div>
                    )}
                </>
            )}

            {view === 'grid' && (
                <div className="card">
                    <div className="card-header">
                        <h3>🗓️ {grid ? `${grid.day} — Rooms × Slots` : 'Day Grid'}</h3>
                        <button className="btn btn-secondary btn-sm" onClick={handleGrid} disabled={loading}>
                            {loading ? 'Refreshing…' : '↻ Refresh'}
                        </button>
                    </div>
                    <div className="card-body">
                        {loading && !grid ? (
                            <div className="loading-container">
                                <div className="spinner" />
                                <div>Loading day grid...</div>
                            </div>
                        ) : grid ? (
                            <>
                                <p className="grid-scroll-hint">← Scroll horizontally to see all time slots →</p>
                                <div className="table-wrapper">
                                    <table className="data-table">
                                        <thead>
                                            <tr>
                                                <th className="sticky-col">Room</th>
                                                {grid.slots.map(s => <th key={s}>{s}</th>)}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {grid.rooms.map(r => (
                                                <tr key={r.roomNumber}>
                                                    <td className="sticky-col">
                                                        <strong>{r.roomNumber}</strong>
                                                        <br />
                                                        <span className="text-muted">{r.type}{r.capacity ? ` · 👥 ${r.capacity}` : ''}</span>
                                                    </td>
                                                    {grid.slots.map(s => {
                                                        const cell = r.slots[s];
                                                        return (
                                                            <td key={s} className={cell ? 'cell-busy' : 'cell-free'}
                                                                title={cell ? `${cell.subject} — ${cell.facultyName || ''}` : `Free — book ${r.roomNumber} at ${s}`}>
                                                                {cell ? (
                                                                    <>✗<small>{cell.subject}</small></>
                                                                ) : canBook ? (
                                                                    <button
                                                                        className="cell-book-btn"
                                                                        onClick={() => openBooking(r.roomNumber, grid.day, s, 'grid')}
                                                                        title={`Book ${r.roomNumber} · ${grid.day} · ${s}`}
                                                                    >
                                                                        ✓ Book
                                                                    </button>
                                                                ) : '✓'}
                                                            </td>
                                                        );
                                                    })}
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                                {grid.rooms.length === 0 && (
                                    <div className="table-empty">
                                        <div className="table-empty-icon">🚪</div>
                                        <div>No rooms in inventory.</div>
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="table-empty">
                                <div className="table-empty-icon">🗓️</div>
                                <div>Pick a day and open this tab to load the grid.</div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {view === 'free' && (
                <div className="card">
                    <div className="card-header">
                        <h3>🕒 Find free slots</h3>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleFree} className="filter-bar">
                            <div className="form-group" style={{ margin: 0 }}>
                                <label className="form-label" htmlFor="free-room">Room</label>
                                <select id="free-room" className="form-control" value={freeRoom} onChange={e => setFreeRoom(e.target.value)}>
                                    {allRooms.map(r => <option key={r.id || r.roomNumber} value={r.roomNumber}>{r.roomNumber}</option>)}
                                </select>
                            </div>
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? 'Loading…' : 'Find'}
                            </button>
                        </form>
                        {freeResult && (
                            <div>
                                <p className="text-muted">
                                    {freeResult.roomNumber} on {freeResult.day}: {freeResult.freeSlots.length} free slot(s)
                                </p>
                                <div className="slot-chips">
                                    {freeResult.freeSlots.map(s => (
                                        <span key={s} className="slot-chip">
                                            ✓ {s}
                                            {canBook && (
                                                <button
                                                    className="chip-book-btn"
                                                    onClick={() => openBooking(freeResult.roomNumber, freeResult.day, s, 'free')}
                                                    title={`Book ${freeResult.roomNumber} · ${freeResult.day} · ${s}`}
                                                >
                                                    Book
                                                </button>
                                            )}
                                        </span>
                                    ))}
                                    {freeResult.freeSlots.length === 0 && <span className="text-muted">Fully booked that day.</span>}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {view === 'manage' && (
                <div className="card">
                    <div className="card-header">
                        <h3>⚙️ Manage rooms</h3>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleCreate} className="manage-form-grid">
                            <div className="form-group">
                                <label className="form-label" htmlFor="new-room-no">Room No *</label>
                                <input id="new-room-no" className="form-control" type="text" value={newRoom.roomNumber} onChange={e => setNewRoom({ ...newRoom, roomNumber: e.target.value })} placeholder="e.g. A-101" />
                            </div>
                            <div className="form-group">
                                <label className="form-label" htmlFor="new-room-bldg">Building</label>
                                <input id="new-room-bldg" className="form-control" type="text" value={newRoom.building} onChange={e => setNewRoom({ ...newRoom, building: e.target.value })} placeholder="Main Block" />
                            </div>
                            <div className="form-group">
                                <label className="form-label" htmlFor="new-room-cap">Capacity</label>
                                <input id="new-room-cap" className="form-control" type="number" min="1" value={newRoom.capacity} onChange={e => setNewRoom({ ...newRoom, capacity: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label className="form-label" htmlFor="new-room-type">Type</label>
                                <select id="new-room-type" className="form-control" value={newRoom.type} onChange={e => setNewRoom({ ...newRoom, type: e.target.value })}>
                                    {ROOM_TYPES.filter(t => t !== 'All').map(t => <option key={t} value={t}>{t}</option>)}
                                </select>
                            </div>
                            <div>
                                <button type="submit" className="btn btn-primary" disabled={loading}>Add Room</button>
                            </div>
                        </form>
                        <div className="table-wrapper">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Room</th>
                                        <th>Building</th>
                                        <th>Capacity</th>
                                        <th>Type</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {allRooms.map(r => (
                                        <tr key={r.id || r.roomNumber}>
                                            <td><strong>{r.roomNumber}</strong></td>
                                            <td>{r.building || '—'}</td>
                                            <td>{r.capacity || '—'}</td>
                                            <td><span className="badge badge-primary">{r.type || '—'}</span></td>
                                            <td>
                                                <div className="table-actions">
                                                    {r.id && <button className="btn btn-danger btn-sm" onClick={() => handleDelete(r.id)}>Delete</button>}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        {allRooms.length === 0 && (
                            <div className="table-empty">
                                <div className="table-empty-icon">🚪</div>
                                <div>No rooms yet — add the first one above.</div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            <BookRoomModal
                booking={booking}
                onClose={() => setBooking(null)}
                onBooked={handleBooked}
            />
        </div>
    );
};

export default RoomAvailabilityPage;

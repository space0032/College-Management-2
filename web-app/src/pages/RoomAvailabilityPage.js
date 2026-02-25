import React, { useState, useEffect } from 'react';
import { getRooms, checkAvailability } from '../services/roomService';

const RoomAvailabilityPage = () => {
    const [rooms, setRooms] = useState([]);
    const [availability, setAvailability] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [searchParams, setSearchParams] = useState({
        day: 'Monday',
        timeSlot: '09:00 - 10:00 AM'
    });

    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const timeSlots = [
        '09:00 - 10:00 AM',
        '10:00 - 11:00 AM',
        '11:00 - 12:00 PM',
        '12:00 - 01:00 PM',
        '01:00 - 02:00 PM',
        '02:00 - 03:00 PM',
        '03:00 - 04:00 PM',
        '04:00 - 05:00 PM'
    ];

    useEffect(() => {
        fetchAllRooms();
    }, []);

    const fetchAllRooms = async () => {
        setLoading(true);
        try {
            const res = await getRooms();
            setRooms(res.data || []);
            // Run an initial availability check for the default timeslot
            handleCheck();
        } catch (err) {
            console.error(err);
            setError('Failed to load room directory.');
        } finally {
            setLoading(false);
        }
    };

    const handleCheck = async (e) => {
        if (e) e.preventDefault();
        setLoading(true);
        try {
            const res = await checkAvailability(searchParams.day, searchParams.timeSlot);
            setAvailability(res.data || []);
            setError(null);
        } catch (err) {
            console.error(err);
            setError('Failed to query room availability.');
        } finally {
            setLoading(false);
        }
    };

    const handleParamChange = (e) => {
        const { name, value } = e.target;
        setSearchParams(prev => ({ ...prev, [name]: value }));
    };

    const renderRoomGrid = (isAvailableFilter) => {
        const filtered = availability.filter(r => r.isAvailable === isAvailableFilter);

        if (filtered.length === 0) {
            return <div className="text-muted" style={{ padding: '20px', textAlign: 'center' }}>No rooms matching this status.</div>;
        }

        return (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '15px' }}>
                {filtered.map(room => (
                    <div key={room.roomNumber} className="card" style={{
                        padding: '15px',
                        borderLeft: `4px solid ${isAvailableFilter ? '#4caf50' : '#f44336'}`,
                        textAlign: 'center'
                    }}>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '5px' }}>
                            {room.roomNumber}
                        </div>
                        <div className="text-muted" style={{ fontSize: '0.9rem' }}>
                            {room.type}
                        </div>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>🚪 Room Availability</h2>
                    <p className="text-muted">Check real-time classroom and laboratory scheduling capacity.</p>
                </div>
            </div>

            <div className="card" style={{ padding: '20px', marginBottom: '30px' }}>
                <h3>Search Parameters</h3>
                <form onSubmit={handleCheck} className="form-grid" style={{ alignItems: 'end' }}>
                    <div className="form-group">
                        <label>Day of Week</label>
                        <select name="day" value={searchParams.day} onChange={handleParamChange}>
                            {days.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Time Slot</label>
                        <select name="timeSlot" value={searchParams.timeSlot} onChange={handleParamChange}>
                            {timeSlots.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </div>
                    <div className="form-group">
                        <button type="submit" className="btn btn-primary" style={{ width: '100%', height: '42px' }}>
                            Check Availability
                        </button>
                    </div>
                </form>
            </div>

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px' }}>Loading real-time availability...</div>
            ) : availability.length > 0 ? (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                        <h3 style={{ margin: 0 }}>Available Rooms <span className="status-badge status-active">{availability.filter(r => r.isAvailable).length}</span></h3>
                    </div>
                    {renderRoomGrid(true)}

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '30px', marginBottom: '15px' }}>
                        <h3 style={{ margin: 0 }}>Occupied Rooms <span className="status-badge status-rejected">{availability.filter(r => !r.isAvailable).length}</span></h3>
                    </div>
                    {renderRoomGrid(false)}
                </div>
            ) : (
                <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
                    <p className="text-muted">Select a day and timeslot to view available rooms.</p>
                </div>
            )}
        </div>
    );
};

export default RoomAvailabilityPage;

import React, { useEffect, useState, useCallback } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getHostels, addHostel, updateHostel, getRooms, addRoom, getAllocations, allocateRoom, vacateRoom, deleteHostel, deleteRoom } from '../services/hostelService';

const HOSTEL_TYPES = ['Boys', 'Girls', 'Co-ed'];

const HostelPage = () => {
  const [tab, setTab] = useState('hostels');
  const [hostels, setHostels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [editTarget, setEditTarget] = useState(null);

  const [hostelForm, setHostelForm] = useState({ name: '', type: 'Boys', totalCapacity: '', wardenName: '', wardenContact: '', address: '' });
  const [roomForm, setRoomForm] = useState({ roomNumber: '', hostelId: '', capacity: '2', floor: '1', roomType: 'AC' });
  const [allocForm, setAllocForm] = useState({ studentId: '', roomId: '', checkInDate: new Date().toISOString().split('T')[0], remarks: '' });

  const [saving, setSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  const userRole = localStorage.getItem('userRole') || 'STUDENT';
  const isAdmin = userRole === 'ADMIN';

  const fetchAll = useCallback(() => {
    setLoading(true);
    Promise.all([getHostels(), getRooms(), getAllocations()])
      .then(([h, r, a]) => {
        setHostels(h.data || []);
        setRooms(r.data || []);
        setAllocations(a.data || []);
      })
      .catch(() => setError('Failed to load data.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const openCreate = () => {
    setEditTarget(null);
    if (tab === 'hostels') setHostelForm({ name: '', type: 'Boys', totalCapacity: '', wardenName: '', wardenContact: '', address: '' });
    if (tab === 'rooms') setRoomForm({ roomNumber: '', hostelId: '', capacity: '2', floor: '1', roomType: 'AC' });
    if (tab === 'allocations') setAllocForm({ studentId: '', roomId: '', checkInDate: new Date().toISOString().split('T')[0], remarks: '' });
    setModalTitle(`Add ${tab.slice(0, -1)}`);
    setModalOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (tab === 'hostels') {
        if (editTarget) await updateHostel(editTarget.id, hostelForm);
        else await addHostel(hostelForm);
      }
      else if (tab === 'rooms') await addRoom(roomForm);
      else await allocateRoom(allocForm);
      setModalOpen(false);
      fetchAll();
    } catch (err) { setError('Failed to save.'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (type, id) => {
    if (!window.confirm(`Delete this ${type}?`)) return;
    try {
      if (type === 'hostel') await deleteHostel(id);
      else await deleteRoom(id);
      fetchAll();
    } catch (err) { alert('Failed to delete. Ensure it has no dependencies.'); }
  };

  const stats = [
    { label: 'Hostels', value: hostels.length, color: '#3182ce', icon: '🏠' },
    { label: 'Total Rooms', value: rooms.length, color: '#48bb78', icon: '🚪' },
    { label: 'Available Slots', value: rooms.reduce((s, r) => s + (r.capacity - r.occupiedCount), 0), color: '#ecc94b', icon: '✨' },
    { label: 'Residents', value: allocations.length, color: '#9f7aea', icon: '👥' },
  ];

  return (
    <div className="page-container" style={{ maxWidth: '1200px', margin: '0 auto' }}>
      <div className="page-header" style={{ marginBottom: '30px' }}>
        <div>
          <h1 className="page-title">🏠 Institutional Housing</h1>
          <p style={{ color: '#718096', margin: 0 }}>Manage campus residence inventory, allocations, and warden assignments.</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={openCreate}>
            + Register {tab === 'hostels' ? 'Hostel' : tab === 'rooms' ? 'Room' : 'Allocation'}
          </button>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {stats.map(s => (
          <div key={s.label} className="stat-card" style={{ display: 'flex', alignItems: 'center', gap: '20px', padding: '24px' }}>
            <div style={{ fontSize: '2.5rem' }}>{s.icon}</div>
            <div>
              <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#2d3748' }}>{s.value}</div>
              <div style={{ color: '#718096', fontSize: '0.9rem', textTransform: 'uppercase' }}>{s.label}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="tab-buttons" style={{ marginBottom: '25px', background: '#edf2f7', padding: '5px', borderRadius: '12px', display: 'inline-flex' }}>
        {['hostels', 'rooms', 'allocations'].map(t => (
          <button key={t} className={`btn-tab ${tab === t ? 'active' : ''}`} onClick={() => setTab(t)} style={{ padding: '10px 25px', borderRadius: '8px' }}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      <div className="stat-card" style={{ padding: '0px', overflow: 'hidden' }}>
        {tab === 'hostels' && (
          <DataTable
            columns={[
              { label: 'Name', key: 'name', render: (v) => <strong>{v}</strong> },
              { label: 'Type', key: 'type', render: (v) => <span className={`badge ${v === 'Boys' ? 'badge-primary' : 'badge-danger'}`}>{v}</span> },
              { label: 'Warden', key: 'wardenName' },
              { label: 'Rooms', key: 'totalRooms' },
              { label: 'Capacity', key: 'totalCapacity' },
              {
                label: 'Actions', key: 'id', render: (_, row) => isAdmin && (
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-sm btn-secondary" onClick={() => {
                      setEditTarget(row);
                      setHostelForm({
                        name: row.name, type: row.type, totalCapacity: row.totalCapacity,
                        wardenName: row.wardenName, wardenContact: row.wardenContact, address: row.address
                      });
                      setModalTitle('Assign/Edit Warden');
                      setModalOpen(true);
                    }}>⚙️ Manage Warden</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete('hostel', row.id)}>Delete</button>
                  </div>
                )
              }
            ]}
            data={hostels}
          />
        )}
        {tab === 'rooms' && (
          <DataTable
            columns={[
              { label: 'Hostel', key: 'hostelName' },
              { label: 'Room #', key: 'roomNumber', render: (v) => <span style={{ fontFamily: 'monospace', fontWeight: 'bold' }}>{v}</span> },
              { label: 'Floor', key: 'floor' },
              { label: 'Type', key: 'roomType' },
              { label: 'Occupancy', key: 'id', render: (_, row) => `${row.occupiedCount} / ${row.capacity}` },
              { label: 'Status', key: 'status', render: (v) => <span className={`badge ${v === 'AVAILABLE' ? 'badge-success' : 'badge-warning'}`}>{v}</span> },
              { label: 'Actions', key: 'id', render: (v) => isAdmin && <button className="btn btn-sm btn-danger" onClick={() => handleDelete('room', v)}>Remove</button> }
            ]}
            data={rooms}
          />
        )}
        {tab === 'allocations' && (
          <DataTable
            columns={[
              { label: 'Student', key: 'studentName' },
              { label: 'Hostel', key: 'hostelName' },
              { label: 'Room', key: 'roomNumber' },
              { label: 'Date', key: 'checkInDate' },
              { label: 'Status', key: 'status', render: (v) => <span className="badge badge-primary">{v}</span> },
              { label: 'Actions', key: 'id', render: (v) => isAdmin && <button className="btn btn-sm btn-danger" onClick={() => vacateRoom(v).then(fetchAll)}>Vacate</button> }
            ]}
            data={allocations}
          />
        )}
      </div>

      <Modal isOpen={modalOpen} title={modalTitle} onClose={() => setModalOpen(false)} onSubmit={handleSave}>
        {tab === 'hostels' ? (
          <div className="form-grid">
            <div className="form-group"><label>Hostel Name</label><input type="text" value={hostelForm.name} onChange={e => setHostelForm({ ...hostelForm, name: e.target.value })} /></div>
            <div className="form-group"><label>Type</label><select value={hostelForm.type} onChange={e => setHostelForm({ ...hostelForm, type: e.target.value })}>{HOSTEL_TYPES.map(t => <option key={t} value={t}>{t}</option>)}</select></div>
            <div className="form-group"><label>Total Capacity</label><input type="number" value={hostelForm.totalCapacity} onChange={e => setHostelForm({ ...hostelForm, totalCapacity: e.target.value })} /></div>
            <div className="form-group"><label>Warden Name</label><input type="text" value={hostelForm.wardenName} onChange={e => setHostelForm({ ...hostelForm, wardenName: e.target.value })} /></div>
            <div className="form-group"><label>Warden Contact</label><input type="text" value={hostelForm.wardenContact} onChange={e => setHostelForm({ ...hostelForm, wardenContact: e.target.value })} /></div>
            <div className="form-group" style={{ gridColumn: '1 / -1' }}><label>Address</label><textarea value={hostelForm.address} onChange={e => setHostelForm({ ...hostelForm, address: e.target.value })} /></div>
          </div>
        ) : tab === 'rooms' ? (
          <div className="form-grid">
            <div className="form-group"><label>Hostel</label><select value={roomForm.hostelId} onChange={e => setRoomForm({ ...roomForm, hostelId: e.target.value })}><option value="">Select Hostel</option>{hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}</select></div>
            <div className="form-group"><label>Room Number</label><input type="text" value={roomForm.roomNumber} onChange={e => setRoomForm({ ...roomForm, roomNumber: e.target.value })} /></div>
            <div className="form-group"><label>Floor</label><input type="number" value={roomForm.floor} onChange={e => setRoomForm({ ...roomForm, floor: e.target.value })} /></div>
            <div className="form-group"><label>Capacity</label><input type="number" value={roomForm.capacity} onChange={e => setRoomForm({ ...roomForm, capacity: e.target.value })} /></div>
            <div className="form-group"><label>Room Type</label><select value={roomForm.roomType} onChange={e => setRoomForm({ ...roomForm, roomType: e.target.value })}><option value="AC">AC</option><option value="Non-AC">Non-AC</option></select></div>
          </div>
        ) : (
          <div className="form-grid">
            <div className="form-group"><label>Student ID</label><input type="number" value={allocForm.studentId} onChange={e => setAllocForm({ ...allocForm, studentId: e.target.value })} /></div>
            <div className="form-group"><label>Room</label><select value={allocForm.roomId} onChange={e => setAllocForm({ ...allocForm, roomId: e.target.value })}><option value="">Select Room</option>{rooms.filter(r => r.occupiedCount < r.capacity).map(r => <option key={r.id} value={r.id}>{r.roomNumber} ({r.hostelName})</option>)}</select></div>
            <div className="form-group"><label>Check-in Date</label><input type="date" value={allocForm.checkInDate} onChange={e => setAllocForm({ ...allocForm, checkInDate: e.target.value })} /></div>
            <div className="form-group" style={{ gridColumn: '1 / -1' }}><label>Remarks</label><textarea value={allocForm.remarks} onChange={e => setAllocForm({ ...allocForm, remarks: e.target.value })} /></div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default HostelPage;

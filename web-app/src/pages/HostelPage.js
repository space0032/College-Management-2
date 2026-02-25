import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getHostels, addHostel, getRooms, addRoom, getAllocations, allocateRoom, vacateRoom } from '../services/hostelService';

const HOSTEL_TYPES = ['Boys', 'Girls', 'Co-ed'];

const HostelPage = () => {
  const [tab, setTab] = useState('hostels');
  const [hostels, setHostels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [editTarget, setEditTarget] = useState(null); // null = create, obj = edit

  // Forms
  const [hostelForm, setHostelForm] = useState({ name: '', type: 'Boys', capacity: '' });
  const [roomForm, setRoomForm] = useState({ roomNumber: '', hostelId: '', hostelName: '', capacity: '', floor: '' });
  const [allocForm, setAllocForm] = useState({ studentId: '', roomId: '', allottedDate: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  const isAdmin = user.role === 'ADMIN';

  const fetchAll = () => {
    setLoading(true);
    Promise.all([getHostels(), getRooms(), getAllocations()])
      .then(([h, r, a]) => {
        setHostels(h.data || []);
        setRooms(r.data || []);
        setAllocations(a.data || []);
      })
      .catch(() => setError('Failed to load hostel data.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const openCreate = () => {
    setEditTarget(null);
    setFormError('');
    if (tab === 'hostels') setHostelForm({ name: '', type: 'Boys', capacity: '' });
    if (tab === 'rooms') setRoomForm({ roomNumber: '', hostelId: '', hostelName: '', capacity: '', floor: '' });
    if (tab === 'allocations') setAllocForm({ studentId: '', roomId: '', allottedDate: '' });
    setModalTitle(`Add ${tab === 'hostels' ? 'Hostel' : tab === 'rooms' ? 'Room' : 'Allocation'}`);
    setModalOpen(true);
  };

  const openEdit = (item) => {
    setEditTarget(item);
    setFormError('');
    if (tab === 'hostels') setHostelForm({ name: item.name, type: item.type || 'Boys', capacity: item.capacity });
    if (tab === 'rooms') setRoomForm({ roomNumber: item.roomNumber, hostelId: item.hostelId || '', hostelName: item.hostelName || '', capacity: item.capacity, floor: item.floor || '' });
    setModalTitle(`Edit ${tab === 'hostels' ? 'Hostel' : 'Room'}`);
    setModalOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    try {
      if (tab === 'hostels') {
        if (!hostelForm.name) { setFormError('Hostel name is required.'); setSaving(false); return; }
        await addHostel(hostelForm); // addHostel is POST, backend should handle update via id presence
      } else if (tab === 'rooms') {
        if (!roomForm.roomNumber) { setFormError('Room number is required.'); setSaving(false); return; }
        // Populate hostelName from selected hostelId if user picked one
        const selectedHostel = hostels.find(h => String(h.id) === String(roomForm.hostelId));
        const payload = { ...roomForm, hostelName: selectedHostel?.name || roomForm.hostelName };
        await addRoom(payload);
      } else {
        if (!allocForm.studentId || !allocForm.roomId) { setFormError('Student ID and Room ID are required.'); setSaving(false); return; }
        await allocateRoom(allocForm);
      }
      setModalOpen(false);
      fetchAll();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save.');
    } finally {
      setSaving(false);
    }
  };

  const handleVacate = async (row) => {
    if (!window.confirm('Vacate this room allocation?')) return;
    try { await vacateRoom(row.id); fetchAll(); } catch { setError('Failed to vacate room.'); }
  };

  // Stats computations
  const totalCapacity = hostels.reduce((s, h) => s + (Number(h.capacity) || 0), 0);
  const occupiedRooms = rooms.filter(r => (r.occupied || 0) >= (r.capacity || 1)).length;
  const availableRooms = rooms.length - occupiedRooms;

  const filteredHostels = searchQuery
    ? hostels.filter(h => (h.name || '').toLowerCase().includes(searchQuery.toLowerCase()) || (h.type || '').toLowerCase().includes(searchQuery.toLowerCase()))
    : hostels;

  const filteredRooms = searchQuery
    ? rooms.filter(r => (r.roomNumber || '').toLowerCase().includes(searchQuery.toLowerCase()) || (r.hostelName || '').toLowerCase().includes(searchQuery.toLowerCase()))
    : rooms;

  const HOSTEL_COLS = [
    { key: 'id', label: 'ID' },
    { key: 'name', label: 'Hostel Name' },
    {
      key: 'type', label: 'Type', render: (v) => (
        <span style={{
          background: v === 'Boys' ? '#ebf8ff' : v === 'Girls' ? '#fff5f7' : '#f0fff4',
          color: v === 'Boys' ? '#2b6cb0' : v === 'Girls' ? '#97266d' : '#276749',
          padding: '2px 10px', borderRadius: '12px', fontSize: '0.78rem', fontWeight: '600'
        }}>{v || 'N/A'}</span>
      )
    },
    { key: 'capacity', label: 'Capacity' },
    ...(isAdmin ? [{
      key: 'actions', label: 'Actions', render: (_, row) => (
        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(row)}>✏️ Edit</button>
      )
    }] : [])
  ];

  const ROOM_COLS = [
    { key: 'id', label: 'ID' },
    { key: 'roomNumber', label: 'Room No.' },
    { key: 'hostelName', label: 'Hostel' },
    { key: 'capacity', label: 'Capacity' },
    {
      key: 'occupied', label: 'Status', render: (v, row) => {
        const isFull = (v || 0) >= (row.capacity || 1);
        return (
          <span style={{
            background: isFull ? '#fff5f5' : '#f0fff4',
            color: isFull ? '#c53030' : '#276749',
            padding: '2px 10px', borderRadius: '12px', fontSize: '0.78rem', fontWeight: '600'
          }}>
            {isFull ? `Full (${v}/${row.capacity})` : `Available (${v || 0}/${row.capacity})`}
          </span>
        );
      }
    },
    ...(isAdmin ? [{
      key: 'actions', label: 'Actions', render: (_, row) => (
        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(row)}>✏️ Edit</button>
      )
    }] : [])
  ];

  const ALLOC_COLS = [
    { key: 'id', label: 'ID' },
    { key: 'studentName', label: 'Student' },
    { key: 'roomNumber', label: 'Room' },
    { key: 'hostelName', label: 'Hostel' },
    { key: 'allottedDate', label: 'Allotted On' },
    ...(isAdmin ? [{
      key: 'actions', label: 'Actions', render: (_, row) => (
        <button className="btn btn-danger btn-sm" onClick={() => handleVacate(row)}>Vacate</button>
      )
    }] : [])
  ];

  const TABS = [
    { key: 'hostels', label: '🏠 Hostels' },
    { key: 'rooms', label: '🚪 Rooms' },
    { key: 'allocations', label: '📝 Allocations' },
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🏠 Hostel Management</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={openCreate}>
            + Add {tab === 'hostels' ? 'Hostel' : tab === 'rooms' ? 'Room' : 'Allocation'}
          </button>
        )}
      </div>

      {/* Stats Row */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {[
          { label: 'Total Hostels', value: hostels.length, color: '#2b6cb0', bg: '#ebf8ff' },
          { label: 'Total Rooms', value: rooms.length, color: '#276749', bg: '#f0fff4' },
          { label: 'Available Rooms', value: availableRooms, color: '#276749', bg: '#f0fff4' },
          { label: 'Occupied Rooms', value: occupiedRooms, color: '#c53030', bg: '#fff5f5' },
          { label: 'Total Capacity', value: totalCapacity, color: '#4a5568', bg: '#f7fafc' },
          { label: 'Current Residents', value: allocations.length, color: '#9f7aea', bg: '#faf5ff' },
        ].map(s => (
          <div key={s.label} style={{ padding: '10px 16px', background: s.bg, border: `1px solid ${s.color}30`, borderRadius: '8px', minWidth: '110px', textAlign: 'center' }}>
            <div style={{ fontWeight: 'bold', fontSize: '1.3rem', color: s.color }}>{s.value}</div>
            <div style={{ fontSize: '0.73rem', color: '#718096' }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Tab bar + Search */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px', flexWrap: 'wrap', gap: '10px' }}>
        <div className="tab-buttons" style={{ margin: 0 }}>
          {TABS.map((t) => (
            <button key={t.key} className={`btn btn-tab ${tab === t.key ? 'active' : ''}`} onClick={() => { setTab(t.key); setSearchQuery(''); }}>{t.label}</button>
          ))}
        </div>
        {(tab === 'hostels' || tab === 'rooms') && (
          <input
            type="text" placeholder={`Search ${tab}…`} value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            style={{ padding: '8px 14px', border: '1px solid #e2e8f0', borderRadius: '8px', width: '220px', fontSize: '0.87rem' }}
          />
        )}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : tab === 'hostels' ? (
        <DataTable columns={HOSTEL_COLS} data={filteredHostels} emptyMessage="No hostels found." />
      ) : tab === 'rooms' ? (
        <DataTable columns={ROOM_COLS} data={filteredRooms} emptyMessage="No rooms found." />
      ) : (
        <DataTable columns={ALLOC_COLS} data={allocations} emptyMessage="No allocations found." />
      )}

      {/* Add / Edit Modal */}
      <Modal isOpen={modalOpen} title={modalTitle} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : (editTarget ? 'Update' : 'Save')}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}

        {tab === 'hostels' && (
          <>
            <div className="form-group">
              <label className="form-label">Hostel Name *</label>
              <input type="text" className="form-control" value={hostelForm.name} onChange={e => setHostelForm(p => ({ ...p, name: e.target.value }))} placeholder="e.g. Saraswati Hostel" />
            </div>
            <div className="form-group">
              <label className="form-label">Type</label>
              <select className="form-control" value={hostelForm.type} onChange={e => setHostelForm(p => ({ ...p, type: e.target.value }))}>
                {HOSTEL_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Total Capacity</label>
              <input type="number" min="1" className="form-control" value={hostelForm.capacity} onChange={e => setHostelForm(p => ({ ...p, capacity: e.target.value }))} placeholder="e.g. 200" />
            </div>
          </>
        )}

        {tab === 'rooms' && (
          <>
            <div className="form-group">
              <label className="form-label">Room Number *</label>
              <input type="text" className="form-control" value={roomForm.roomNumber} onChange={e => setRoomForm(p => ({ ...p, roomNumber: e.target.value }))} placeholder="e.g. A-101" />
            </div>
            <div className="form-group">
              <label className="form-label">Hostel</label>
              <select className="form-control" value={roomForm.hostelId} onChange={e => setRoomForm(p => ({ ...p, hostelId: e.target.value }))}>
                <option value="">-- Select Hostel --</option>
                {hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Capacity</label>
              <input type="number" min="1" className="form-control" value={roomForm.capacity} onChange={e => setRoomForm(p => ({ ...p, capacity: e.target.value }))} placeholder="e.g. 3" />
            </div>
            <div className="form-group">
              <label className="form-label">Floor</label>
              <input type="text" className="form-control" value={roomForm.floor} onChange={e => setRoomForm(p => ({ ...p, floor: e.target.value }))} placeholder="e.g. Ground / 1st" />
            </div>
          </>
        )}

        {tab === 'allocations' && (
          <>
            <div className="form-group">
              <label className="form-label">Student ID *</label>
              <input type="number" className="form-control" value={allocForm.studentId} onChange={e => setAllocForm(p => ({ ...p, studentId: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Room *</label>
              <select className="form-control" value={allocForm.roomId} onChange={e => setAllocForm(p => ({ ...p, roomId: e.target.value }))}>
                <option value="">-- Select Room --</option>
                {rooms.filter(r => (r.occupied || 0) < (r.capacity || 1)).map(r => (
                  <option key={r.id} value={r.id}>{r.roomNumber} ({r.hostelName}) — {r.capacity - (r.occupied || 0)} spots left</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Allotted Date</label>
              <input type="date" className="form-control" value={allocForm.allottedDate} onChange={e => setAllocForm(p => ({ ...p, allottedDate: e.target.value }))} />
            </div>
          </>
        )}
      </Modal>
    </div>
  );
};

export default HostelPage;

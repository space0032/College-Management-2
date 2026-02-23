import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getHostels, addHostel, getRooms, addRoom, getAllocations, allocateRoom, vacateRoom } from '../services/hostelService';

const HOSTEL_COLS = [{ key: 'id', label: 'ID' }, { key: 'name', label: 'Hostel Name' }, { key: 'type', label: 'Type' }, { key: 'capacity', label: 'Capacity' }];
const ROOM_COLS = [{ key: 'id', label: 'ID' }, { key: 'roomNumber', label: 'Room No.' }, { key: 'hostelName', label: 'Hostel' }, { key: 'capacity', label: 'Capacity' }, { key: 'occupied', label: 'Occupied' }];
const ALLOC_COLS = [{ key: 'id', label: 'ID' }, { key: 'studentName', label: 'Student' }, { key: 'roomNumber', label: 'Room' }, { key: 'hostelName', label: 'Hostel' }, { key: 'allottedDate', label: 'Allotted On' }];

const HostelPage = () => {
  const [tab, setTab] = useState('hostels');
  const [hostels, setHostels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [hostelForm, setHostelForm] = useState({ name: '', type: '', capacity: '' });
  const [roomForm, setRoomForm] = useState({ roomNumber: '', hostelName: '', capacity: '' });
  const [allocForm, setAllocForm] = useState({ studentId: '', roomId: '', allottedDate: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchAll = () => {
    setLoading(true);
    Promise.all([getHostels(), getRooms(), getAllocations()])
      .then(([h, r, a]) => { setHostels(h.data || []); setRooms(r.data || []); setAllocations(a.data || []); })
      .catch(() => setError('Failed to load hostel data.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      if (tab === 'hostels') {
        if (!hostelForm.name) { setFormError('Hostel name is required.'); setSaving(false); return; }
        await addHostel(hostelForm);
      } else if (tab === 'rooms') {
        if (!roomForm.roomNumber) { setFormError('Room number is required.'); setSaving(false); return; }
        await addRoom(roomForm);
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

  const openModal = () => { setFormError(''); setModalOpen(true); };

  const TABS = [
    { key: 'hostels', label: '🏠 Hostels' },
    { key: 'rooms', label: '🚪 Rooms' },
    { key: 'allocations', label: '📝 Allocations' },
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🏠 Hostel Management</h1>
        <button className="btn btn-primary" onClick={openModal}>
          + Add {tab === 'hostels' ? 'Hostel' : tab === 'rooms' ? 'Room' : 'Allocation'}
        </button>
      </div>

      <div className="tab-buttons">
        {TABS.map((t) => (
          <button key={t.key} className={`btn btn-tab ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>{t.label}</button>
        ))}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : tab === 'hostels' ? (
        <DataTable columns={HOSTEL_COLS} data={hostels} emptyMessage="No hostels found." />
      ) : tab === 'rooms' ? (
        <DataTable columns={ROOM_COLS} data={rooms} emptyMessage="No rooms found." />
      ) : (
        <DataTable columns={ALLOC_COLS} data={allocations} onDelete={handleVacate} emptyMessage="No allocations found." />
      )}

      <Modal isOpen={modalOpen} title={`Add ${tab === 'hostels' ? 'Hostel' : tab === 'rooms' ? 'Room' : 'Allocation'}`} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {tab === 'hostels' && (
          <>
            {[{ name: 'name', label: 'Hostel Name' }, { name: 'type', label: 'Type (Boys/Girls)' }, { name: 'capacity', label: 'Capacity' }].map(({ name, label }) => (
              <div className="form-group" key={name}>
                <label className="form-label">{label}</label>
                <input name={name} type={name === 'capacity' ? 'number' : 'text'} className="form-control" value={hostelForm[name]} onChange={(e) => setHostelForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={`Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        )}
        {tab === 'rooms' && (
          <>
            {[{ name: 'roomNumber', label: 'Room Number' }, { name: 'hostelName', label: 'Hostel Name' }, { name: 'capacity', label: 'Capacity' }].map(({ name, label }) => (
              <div className="form-group" key={name}>
                <label className="form-label">{label}</label>
                <input name={name} type={name === 'capacity' ? 'number' : 'text'} className="form-control" value={roomForm[name]} onChange={(e) => setRoomForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={`Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        )}
        {tab === 'allocations' && (
          <>
            {[{ name: 'studentId', label: 'Student ID' }, { name: 'roomId', label: 'Room ID' }, { name: 'allottedDate', label: 'Allotted Date', type: 'date' }].map(({ name, label, type = 'text' }) => (
              <div className="form-group" key={name}>
                <label className="form-label">{label}</label>
                <input name={name} type={type} className="form-control" value={allocForm[name]} onChange={(e) => setAllocForm((p) => ({ ...p, [name]: e.target.value }))} />
              </div>
            ))}
          </>
        )}
      </Modal>
    </div>
  );
};

export default HostelPage;

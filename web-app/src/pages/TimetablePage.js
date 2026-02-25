import React, { useState } from 'react';
import Modal from '../components/Modal';
import { getTimetable, saveTimetableEntry, deleteTimetableEntry } from '../services/timetableService';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const EMPTY_FORM = { day: '', timeSlot: '', subject: '', room: '', faculty: '' };

const SUBJECT_COLORS = [
  '#ebf8ff', '#f0fff4', '#fffaf0', '#fef5ff', '#fff5f5', '#f0f9ff',
  '#faf5ff', '#fffde4', '#e6fffa', '#fff0f3',
];

const getSubjectColor = (subject) => {
  let hash = 0;
  for (let i = 0; i < (subject || '').length; i++) hash = subject.charCodeAt(i) + ((hash << 5) - hash);
  return SUBJECT_COLORS[Math.abs(hash) % SUBJECT_COLORS.length];
};

const TimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dept, setDept] = useState('');
  const [semester, setSemester] = useState('');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'list'
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  const isAdmin = user.role === 'ADMIN' || user.role === 'FACULTY';

  const handleFetch = async () => {
    if (!dept || !semester) { setError('Please enter both Department and Semester.'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await getTimetable(dept, semester);
      setEntries(res.data || []);
    } catch {
      setError('Failed to load timetable.');
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const handleSave = async () => {
    if (!form.day || !form.timeSlot || !form.subject) { setFormError('Day, time slot, and subject are required.'); return; }

    // Conflict Detection
    const conflict = entries.find(e => e.day === form.day && e.timeSlot === form.timeSlot);
    if (conflict) {
      setFormError(`Conflict Detected: The slot ${form.timeSlot} on ${form.day} is already scheduled with ${conflict.subject}. Please choose a different time or delete the existing entry first.`);
      return;
    }

    setSaving(true);
    try {
      await saveTimetableEntry({ ...form, department: dept, semester });
      setModalOpen(false);
      setForm(EMPTY_FORM);
      if (dept && semester) handleFetch();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save entry.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (entry) => {
    if (!window.confirm('Delete this timetable entry?')) return;
    try {
      await deleteTimetableEntry(entry.id);
      setEntries((prev) => prev.filter((e) => e.id !== entry.id));
    } catch {
      setError('Failed to delete entry.');
    }
  };

  // Build grid structure: { day -> [entries sorted by timeSlot] }
  const gridData = DAYS.reduce((acc, day) => {
    acc[day] = entries.filter(e => e.day === day).sort((a, b) => (a.timeSlot || '').localeCompare(b.timeSlot || ''));
    return acc;
  }, {});

  // Unique time slots across all days for column headers
  const allTimeSlots = [...new Set(entries.map(e => e.timeSlot).filter(Boolean))].sort();

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🗓️ Timetable</h1>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          {/* View toggle */}
          <div style={{ display: 'flex', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden' }}>
            {[{ id: 'grid', label: '⊞ Grid' }, { id: 'list', label: '☰ List' }].map(v => (
              <button key={v.id} onClick={() => setViewMode(v.id)} style={{
                padding: '6px 14px', border: 'none', cursor: 'pointer', fontSize: '0.82rem', fontWeight: '500',
                background: viewMode === v.id ? '#3b82f6' : 'white',
                color: viewMode === v.id ? 'white' : '#4a5568'
              }}>{v.label}</button>
            ))}
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setFormError(''); setModalOpen(true); }}>
              + Add Entry
            </button>
          )}
        </div>
      </div>

      {/* Filter bar */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', alignItems: 'center', flexWrap: 'wrap' }}>
        <input type="text" className="form-control" placeholder="Department (e.g. CS)" value={dept} onChange={(e) => setDept(e.target.value)} style={{ maxWidth: '200px' }} />
        <input type="number" min="1" max="8" className="form-control" placeholder="Semester (1-8)" value={semester} onChange={(e) => setSemester(e.target.value)} style={{ maxWidth: '160px' }} />
        <button className="btn btn-primary" onClick={handleFetch}>Load Timetable</button>
        {entries.length > 0 && (
          <span style={{ fontSize: '0.82rem', color: '#718096' }}>{entries.length} class{entries.length !== 1 ? 'es' : ''} loaded</span>
        )}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading timetable…</span></div>
      ) : entries.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '60px', color: '#a0aec0' }}>
          <div style={{ fontSize: '3rem', marginBottom: '12px' }}>📅</div>
          <div style={{ fontSize: '1rem' }}>Enter department and semester above, then click Load.</div>
        </div>
      ) : viewMode === 'grid' ? (
        /* ===== VISUAL WEEKLY GRID ===== */
        <div style={{ overflowX: 'auto' }}>
          {allTimeSlots.length > 0 ? (
            /* Time-slot × Day matrix */
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '700px' }}>
              <thead>
                <tr style={{ background: 'linear-gradient(135deg, #667eea, #764ba2)', color: 'white' }}>
                  <th style={{ padding: '10px 14px', textAlign: 'left', fontSize: '0.82rem', fontWeight: '600', width: '120px' }}>
                    Time Slot
                  </th>
                  {DAYS.filter(d => gridData[d].length > 0).map(day => (
                    <th key={day} style={{ padding: '10px 14px', textAlign: 'center', fontSize: '0.82rem', fontWeight: '600' }}>{day}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {allTimeSlots.map((slot, si) => (
                  <tr key={slot} style={{ background: si % 2 === 0 ? 'white' : '#f8fafc' }}>
                    <td style={{ padding: '8px 14px', fontWeight: '600', fontSize: '0.82rem', color: '#4a5568', borderRight: '2px solid #e2e8f0', whiteSpace: 'nowrap' }}>
                      🕐 {slot}
                    </td>
                    {DAYS.filter(d => gridData[d].length > 0).map(day => {
                      const cell = gridData[day].find(e => e.timeSlot === slot);
                      return (
                        <td key={day} style={{ padding: '6px 10px', verticalAlign: 'top', border: '1px solid #edf2f7' }}>
                          {cell ? (
                            <div style={{
                              background: getSubjectColor(cell.subject),
                              border: '1px solid #e2e8f0',
                              borderRadius: '8px',
                              padding: '8px 10px',
                              minHeight: '60px',
                              position: 'relative'
                            }}>
                              <div style={{ fontWeight: '700', fontSize: '0.85rem', color: '#2d3748', marginBottom: '2px' }}>{cell.subject}</div>
                              {cell.faculty && <div style={{ fontSize: '0.73rem', color: '#718096' }}>👩‍🏫 {cell.faculty}</div>}
                              {cell.room && <div style={{ fontSize: '0.73rem', color: '#718096' }}>🚪 {cell.room}</div>}
                              {isAdmin && (
                                <button onClick={() => handleDelete(cell)} title="Delete" style={{
                                  position: 'absolute', top: '4px', right: '4px',
                                  background: 'none', border: 'none', cursor: 'pointer',
                                  fontSize: '0.75rem', color: '#e53e3e', opacity: 0.6,
                                  lineHeight: 1
                                }}>✕</button>
                              )}
                            </div>
                          ) : (
                            <div style={{ minHeight: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#cbd5e0', fontSize: '0.8rem' }}>—</div>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            /* Day-based card layout if no consistent time slots */
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '16px' }}>
              {DAYS.map(day => (
                gridData[day].length > 0 && (
                  <div key={day}>
                    <div style={{ fontWeight: '700', color: '#4a5568', marginBottom: '8px', padding: '6px 10px', background: '#f7fafc', borderRadius: '6px', fontSize: '0.85rem' }}>
                      {day}
                    </div>
                    {gridData[day].map(e => (
                      <div key={e.id} style={{ background: getSubjectColor(e.subject), border: '1px solid #e2e8f0', borderRadius: '8px', padding: '10px', marginBottom: '8px' }}>
                        <div style={{ fontWeight: '700', fontSize: '0.88rem', color: '#2d3748' }}>{e.subject}</div>
                        <div style={{ fontSize: '0.73rem', color: '#718096' }}>⏰ {e.timeSlot}</div>
                        {e.faculty && <div style={{ fontSize: '0.73rem', color: '#718096' }}>👩‍🏫 {e.faculty}</div>}
                        {e.room && <div style={{ fontSize: '0.73rem', color: '#718096' }}>🚪 {e.room}</div>}
                      </div>
                    ))}
                  </div>
                )
              ))}
            </div>
          )}
        </div>
      ) : (
        /* ===== LIST VIEW ===== */
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Day</th><th>Time Slot</th><th>Subject</th><th>Faculty</th><th>Room</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {DAYS.flatMap(day => gridData[day].map(e => (
                <tr key={e.id}>
                  <td><span style={{ fontWeight: '600', color: '#4a5568' }}>{e.day}</span></td>
                  <td style={{ whiteSpace: 'nowrap' }}>{e.timeSlot}</td>
                  <td>
                    <span style={{ background: getSubjectColor(e.subject), padding: '2px 10px', borderRadius: '12px', fontSize: '0.82rem', fontWeight: '600' }}>
                      {e.subject}
                    </span>
                  </td>
                  <td>{e.faculty || '—'}</td>
                  <td>{e.room || '—'}</td>
                  {isAdmin && (
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(e)}>Delete</button>
                    </td>
                  )}
                </tr>
              )))}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={modalOpen} title="Add Timetable Entry" onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Day *</label>
          <select name="day" className="form-control" value={form.day} onChange={handleFormChange}>
            <option value="">Select day</option>
            {DAYS.map((d) => <option key={d} value={d}>{d}</option>)}
          </select>
        </div>
        {[
          { name: 'timeSlot', label: 'Time Slot', placeholder: 'e.g. 09:00 - 10:00' },
          { name: 'subject', label: 'Subject' },
          { name: 'room', label: 'Room' },
          { name: 'faculty', label: 'Faculty' }
        ].map(({ name, label, placeholder }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input name={name} type="text" className="form-control" value={form[name]} onChange={handleFormChange} placeholder={placeholder || `Enter ${label.toLowerCase()}`} />
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default TimetablePage;

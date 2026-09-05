import React, { useState, useCallback, useEffect, useMemo } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAttendance, markAttendance, bulkMarkAttendance, getCourseStats } from '../services/attendanceService';
import { getAllStudents } from '../services/studentService';
import { getAllCourses } from '../services/courseService';
import { getEnrolledStudents } from '../services/featureService';
import { exportToCSV, exportToExcel } from '../utils/exportUtils';
import { BarChart, Bar, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, ReferenceLine } from 'recharts';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import { CONFIG } from '../config';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'studentId', label: 'Enrollment No.', render: (v, r) => (
    <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>
      {r.enrollmentId || r.enrollmentNumber || r.username || v || 'N/A'}
    </span>
  )},
  { key: 'studentName', label: 'Student' },
  { key: 'courseId', label: 'Subject' },
  { key: 'date', label: 'Date' },
  { key: 'status', label: 'Status', render: (v) => (
    <span style={{
      padding: '2px 8px', borderRadius: 12, fontSize: '0.75rem', fontWeight: 600,
      background: v === 'PRESENT' ? '#dcfce7' : v === 'LATE' ? '#fef9c3' : '#fee2e2',
      color: v === 'PRESENT' ? '#166534' : v === 'LATE' ? '#854d0e' : '#991b1b'
    }}>{v || '—'}</span>
  )},
  { key: 'remarks', label: 'Remarks', render: (v) => v || '—' },
];

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE'];
const todayStr = () => new Date().toISOString().split('T')[0];

const AttendancePage = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [listLoading, setListLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [filterCourse, setFilterCourse] = useState('');
  const [filterDate, setFilterDate] = useState(todayStr());
  const [markModal, setMarkModal] = useState(false);
  const [bulkModal, setBulkModal] = useState(false);
  const [markForm, setMarkForm] = useState({ enrollmentId: '', courseId: '', date: todayStr(), status: 'PRESENT', remarks: '' });
  const [bulkForm, setBulkForm] = useState({ courseId: '', date: todayStr() });
  const [bulkStudents, setBulkStudents] = useState([]); // {id,name,username,status,remarks,alreadyMarked}
  const [bulkSearch, setBulkSearch] = useState('');
  const [formError, setFormError] = useState('');
  const [bulkResult, setBulkResult] = useState(null);
  const [saving, setSaving] = useState(false);
  const [stats, setStats] = useState(null);
  const [threshold, setThreshold] = useState(CONFIG.ACADEMICS.MIN_ATTENDANCE_PERCENTAGE);
  const [students, setStudents] = useState([]);
  const [subjects, setSubjects] = useState([]);

  useEffect(() => {
    getAllStudents().then(res => setStudents((res.data || []).map(s => ({ id: s.id, name: s.name, username: s.username || s.enrollmentId })))).catch(() => {});
    getAllCourses(1, 500).then(res => setSubjects(res.data || [])).catch(() => {});
  }, []);

  const subjectLabel = useCallback((id) => {
    const c = subjects.find(x => String(x.id) === String(id));
    return c ? `${c.code} — ${c.name}` : `Subject ${id}`;
  }, [subjects]);

  const handleFetch = useCallback(async () => {
    if (!filterCourse || !filterDate) { setError('Please select both Subject and Date.'); return; }
    if (filterDate > todayStr()) { setError('Attendance cannot be viewed for a future date.'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await getAttendance(filterCourse, filterDate);
      setRecords(res.data || []);
      try {
        const statsRes = await getCourseStats(filterCourse);
        setStats(statsRes.data.stats || []);
        if (statsRes.data.threshold) setThreshold(statsRes.data.threshold);
      } catch (err) {
        console.error("Failed to fetch stats", err);
        setStats(null);
      }
    } catch {
      setError('Failed to fetch attendance records.');
    } finally {
      setLoading(false);
    }
  }, [filterCourse, filterDate]);

  const handleMark = async () => {
    if (!markForm.enrollmentId || !markForm.courseId || !markForm.date) {
      setFormError('Student, Subject and Date are required.');
      return;
    }
    if (markForm.date > todayStr()) {
      setFormError('Attendance cannot be marked for a future date.');
      return;
    }
    setSaving(true);
    setFormError('');
    try {
      await markAttendance(markForm);
      setNotice(`Marked ${markForm.status} for ${markForm.enrollmentId}.`);
      setMarkModal(false);
      // Keep course/date context for rapid entry; only clear student.
      setMarkForm((p) => ({ ...p, enrollmentId: '', status: 'PRESENT', remarks: '' }));
      if (String(filterCourse) === String(markForm.courseId) && filterDate === markForm.date) handleFetch();
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to mark attendance.');
    } finally {
      setSaving(false);
    }
  };

  const loadBulkList = useCallback(async (courseId, date) => {
    if (!courseId) { setFormError('Select a subject first.'); return; }
    setListLoading(true);
    setFormError('');
    setBulkResult(null);
    try {
      const res = await getEnrolledStudents(courseId);
      let enrolled = res.data || [];
      if (enrolled.length === 0) {
        const all = await getAllStudents();
        enrolled = (all.data || []).map(s => ({ id: s.id, name: s.name, username: s.username || s.enrollmentId }));
      }
      const byId = new Map((students || []).map(s => [s.id, s]));
      let existing = [];
      if (date) {
        try {
          const att = await getAttendance(courseId, date);
          existing = att.data || [];
        } catch { /* no existing yet */ }
      }
      const existingByStudent = new Map(existing.map(r => [r.studentId, r]));
      setBulkStudents(enrolled.map(s => {
        const prev = existingByStudent.get(s.id);
        return {
          ...s,
          username: s.username || byId.get(s.id)?.username,
          name: s.name || byId.get(s.id)?.name,
          status: prev ? prev.status : 'PRESENT',
          remarks: prev ? (prev.remarks || '') : '',
          alreadyMarked: !!prev,
        };
      }));
    } catch {
      setFormError('Failed to load student list.');
    } finally {
      setListLoading(false);
    }
  }, [students]);

  // Auto-load when bulk modal opens with context
  useEffect(() => {
    if (bulkModal && bulkForm.courseId) loadBulkList(bulkForm.courseId, bulkForm.date);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bulkModal]);

  const openBulk = () => {
    setBulkForm({ courseId: filterCourse || '', date: filterDate || todayStr() });
    setBulkSearch('');
    setBulkResult(null);
    setFormError('');
    setBulkStudents([]);
    setBulkModal(true);
  };

  const handleBulk = async () => {
    if (!bulkForm.courseId || !bulkForm.date || bulkStudents.length === 0) {
      setFormError('Subject, Date, and a loaded class list are required. Click “Load Class List” first.');
      return;
    }
    if (bulkForm.date > todayStr()) {
      setFormError('Attendance cannot be marked for a future date.');
      return;
    }
    const bad = bulkStudents.filter(s => !s.username && !(s.enrollmentId));
    if (bad.length > 0) {
      setFormError(`${bad.length} row(s) have no enrollment ID and will be skipped.`);
    }
    setSaving(true);
    setBulkResult(null);
    try {
      const payload = bulkStudents
        .filter(s => s.username || s.enrollmentId)
        .map(s => ({
          enrollmentId: s.username || s.enrollmentId,
          courseId: bulkForm.courseId,
          date: bulkForm.date,
          status: s.status,
          remarks: s.remarks || undefined,
        }));

      const res = await bulkMarkAttendance(payload);
      const marked = res.data.marked ?? payload.length;
      const failed = res.data.failed || [];
      setBulkResult({ marked, failed });
      setNotice(`Bulk saved: ${marked} marked${failed.length ? `, ${failed.length} failed` : ''} for ${subjectLabel(bulkForm.courseId)} on ${bulkForm.date}.`);
      if (failed.length === 0) {
        setBulkModal(false);
        setBulkStudents([]);
        if (String(filterCourse) === String(bulkForm.courseId) && filterDate === bulkForm.date) handleFetch();
      }
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to bulk mark attendance.');
    } finally {
      setSaving(false);
    }
  };

  const handleBulkStatusChange = useCallback((id, status) => {
    setBulkStudents(prev => prev.map(s => s.id === id ? { ...s, status } : s));
  }, []);

  const handleBulkRemarksChange = useCallback((id, remarks) => {
    setBulkStudents(prev => prev.map(s => s.id === id ? { ...s, remarks } : s));
  }, []);

  const markAll = useCallback((status) => {
    setBulkStudents(prev => prev.map(s => ({ ...s, status })));
  }, []);

  const counts = useMemo(() => {
    const c = { PRESENT: 0, ABSENT: 0, LATE: 0 };
    bulkStudents.forEach(s => { if (c[s.status] !== undefined) c[s.status] += 1; });
    return c;
  }, [bulkStudents]);

  const visibleBulk = useMemo(() => {
    const q = bulkSearch.trim().toLowerCase();
    if (!q) return bulkStudents;
    return bulkStudents.filter(s => (s.name || '').toLowerCase().includes(q) || (s.username || '').toLowerCase().includes(q));
  }, [bulkStudents, bulkSearch]);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📋 Attendance</h1>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => { setMarkForm({ enrollmentId: '', courseId: filterCourse || '', date: filterDate || todayStr(), status: 'PRESENT', remarks: '' }); setFormError(''); setMarkModal(true); }}>
            Mark Attendance
          </button>
          <button className="btn btn-secondary" onClick={openBulk}>
            Bulk Mark
          </button>
          {records.length > 0 && (
            <>
              <button className="btn btn-secondary" onClick={() => exportToCSV(
                ['Enrollment No.', 'Student', 'Subject', 'Date', 'Status', 'Remarks'],
                records.map(r => [r.enrollmentId || r.enrollmentNumber || r.username || r.studentId || 'N/A', r.studentName || '', subjectLabel(r.courseId), r.date, r.status, r.remarks || '']),
                'attendance_export'
              )}              >⬇ Export CSV</button>
              <button className="btn btn-secondary" onClick={() => exportToExcel(
                ['Enrollment No.', 'Student', 'Subject', 'Date', 'Status', 'Remarks'],
                records.map(r => [r.enrollmentId || r.enrollmentNumber || r.username || r.studentId || 'N/A', r.studentName || '', subjectLabel(r.courseId), r.date, r.status, r.remarks || '']),
                'attendance_export'
              )}>⬇ Export Excel</button>
              <button className="btn btn-secondary" onClick={() => {
                const doc = new jsPDF();
                doc.setFontSize(16);
                doc.text('Attendance Report', 14, 18);
                doc.setFontSize(10);
                doc.setTextColor(100);
                doc.text(`Course: ${subjectLabel(filterCourse)}   Date: ${filterDate}   Generated: ${new Date().toLocaleString()}`, 14, 26);
                const present = records.filter(r => r.status === 'PRESENT').length;
                doc.text(`Present: ${present}  Absent: ${records.filter(r => r.status === 'ABSENT').length}  Late: ${records.filter(r => r.status === 'LATE').length}  Total: ${records.length}`, 14, 33);
                doc.autoTable({
                  startY: 38,
                  head: [['Enrollment No.', 'Student', 'Subject', 'Date', 'Status', 'Remarks']],
                  body: records.map(r => [r.enrollmentId || r.enrollmentNumber || r.username || r.studentId || 'N/A', r.studentName || '', subjectLabel(r.courseId), r.date, r.status, r.remarks || '']),
                  styles: { fontSize: 9 },
                  headStyles: { fillColor: [59, 130, 246] },
                  alternateRowStyles: { fillColor: [248, 250, 252] }
                });
                doc.save(`attendance_${filterCourse}_${filterDate}.pdf`);
              }}>📄 Export PDF</button>
            </>
          )}
        </div>
      </div>

      <div className="filter-bar">
        <select
          required
          className="form-control"
          value={filterCourse}
          onChange={(e) => setFilterCourse(e.target.value)}
        >
          <option value="">Select subject…</option>
          {subjects.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}{c.specialization ? ` [${c.specialization}]` : ''}</option>)}
        </select>
        <input
          type="date"
          required
          max={todayStr()}
          className="form-control"
          value={filterDate}
          onChange={(e) => setFilterDate(e.target.value)}
        />
        <button className="btn btn-primary btn-sm" onClick={handleFetch}>Fetch</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}
      {notice && <div className="alert alert-success" style={{ marginBottom: 16 }}>{notice} <button className="btn btn-sm" style={{ marginLeft: 8 }} onClick={() => setNotice('')}>Dismiss</button></div>}

      {stats && stats.length > 0 && (
        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h3>📊 Attendance Analytics</h3>
          </div>
          <div className="card-body">
            <div style={{ width: '100%', height: 250, marginBottom: '20px' }}>
              <ResponsiveContainer>
                <BarChart data={stats} margin={{ top: 20, right: 30, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="studentName" axisLine={false} tickLine={false} tick={{ fontSize: 10 }} />
                  <YAxis axisLine={false} tickLine={false} domain={[0, 100]} />
                  <Tooltip cursor={{ fill: '#f1f5f9' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                  <ReferenceLine y={threshold} stroke="#ef4444" strokeDasharray="3 3" label={{ position: 'insideTopLeft', value: `Min ${threshold}%`, fill: '#ef4444', fontSize: 12 }} />
                  <Bar dataKey="percentage" radius={[4, 4, 0, 0]} barSize={40}>
                    {
                      stats.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.isLow ? '#ef4444' : '#10b981'} />
                      ))
                    }
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
            <div style={{ display: 'flex', gap: 16, overflowX: 'auto', paddingBottom: 8 }}>
              {stats.map(s => (
                <StudentStatCard key={s.studentId} s={s} />
              ))}
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading attendance…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={records} emptyMessage="No attendance records. Select a course and date to load." />
      )}

      <Modal isOpen={markModal} title="Mark Attendance" onClose={() => setMarkModal(false)} onSubmit={handleMark} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Subject</label>
          <select className="form-control" value={markForm.courseId} onChange={(e) => setMarkForm((p) => ({ ...p, courseId: e.target.value }))} required>
            <option value="">Select subject…</option>
            {subjects.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Date</label>
          <input type="date" required max={todayStr()} className="form-control" value={markForm.date} onChange={(e) => setMarkForm((p) => ({ ...p, date: e.target.value }))} />
        </div>
        <div className="form-group">
          <label className="form-label">Enrollment No.</label>
          <select className="form-control" value={markForm.enrollmentId} onChange={(e) => setMarkForm((p) => ({ ...p, enrollmentId: e.target.value }))} required>
            <option value="">Select student / enrollment…</option>
            {students.map(s => <option key={s.id} value={s.username}>{s.name} ({s.username})</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-control" value={markForm.status} onChange={(e) => setMarkForm((p) => ({ ...p, status: e.target.value }))}>
            <option value="PRESENT">Present</option>
            <option value="ABSENT">Absent</option>
            <option value="LATE">Late</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Remarks (optional)</label>
          <input type="text" className="form-control" placeholder="e.g. late bus, medical" value={markForm.remarks} onChange={(e) => setMarkForm((p) => ({ ...p, remarks: e.target.value }))} maxLength={255} />
        </div>
      </Modal>

      <Modal isOpen={bulkModal} title="Bulk Mark Attendance" onClose={() => setBulkModal(false)} onSubmit={handleBulk} submitLabel={saving ? 'Saving…' : `Submit Batch (${bulkStudents.length})`}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {bulkResult && (
          <div className="alert alert-success" style={{ marginBottom: 12 }}>
            Marked {bulkResult.marked} record(s).
            {bulkResult.failed?.length > 0 && <span> {bulkResult.failed.length} failed: {bulkResult.failed.map(f => `#${f.index} (${f.reason})`).join(', ')}</span>}
          </div>
        )}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '12px', flexWrap: 'wrap' }}>
          <div className="form-group" style={{ flex: 2, minWidth: 200 }}>
            <label className="form-label">Subject</label>
            <select required className="form-control" value={bulkForm.courseId} onChange={(e) => setBulkForm((p) => ({ ...p, courseId: e.target.value }))}>
              <option value="">Select subject…</option>
              {subjects.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}</option>)}
            </select>
          </div>
          <div className="form-group" style={{ flex: 1, minWidth: 140 }}>
            <label className="form-label">Date</label>
            <input type="date" required max={todayStr()} className="form-control" value={bulkForm.date} onChange={(e) => setBulkForm((p) => ({ ...p, date: e.target.value }))} />
          </div>
          <div style={{ alignSelf: 'flex-end', paddingBottom: '16px' }}>
            <button type="button" className="btn btn-secondary btn-sm" disabled={!bulkForm.courseId || listLoading} onClick={() => loadBulkList(bulkForm.courseId, bulkForm.date)}>
              {listLoading ? 'Loading…' : 'Load Class List'}
            </button>
          </div>
        </div>

        {bulkStudents.length > 0 && (
          <>
            <div style={{ display: 'flex', gap: 8, marginBottom: 10, flexWrap: 'wrap', alignItems: 'center' }}>
              <button type="button" className="btn btn-sm btn-secondary" onClick={() => markAll('PRESENT')}>✓ All Present</button>
              <button type="button" className="btn btn-sm btn-secondary" onClick={() => markAll('ABSENT')}>✗ All Absent</button>
              <span style={{ fontSize: '0.8rem', color: '#475569' }}>✅ {counts.PRESENT} · ❌ {counts.ABSENT} · ⏰ {counts.LATE} · Total {bulkStudents.length}</span>
              <input type="text" placeholder="Search name / enrollment…" value={bulkSearch} onChange={(e) => setBulkSearch(e.target.value)} className="form-control" style={{ marginLeft: 'auto', maxWidth: 220 }} />
            </div>
            <div style={{ maxHeight: '320px', overflowY: 'auto', border: '1px solid #edf2f7', borderRadius: '8px' }}>
              <table className="data-table" style={{ fontSize: '0.85rem' }}>
                <thead style={{ position: 'sticky', top: 0, zIndex: 1, background: 'white' }}>
                  <tr><th>Student</th><th>Status</th><th>Remarks</th><th /></tr>
                </thead>
                <tbody>
                  {visibleBulk.map((s) => (
                    <BulkAttendanceRow
                      key={s.id}
                      student={s}
                      onStatus={handleBulkStatusChange}
                      onRemarks={handleBulkRemarksChange}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </Modal>
    </div>
  );
};

// Memoized Stat Card for better scroll performance
const StudentStatCard = React.memo(({ s }) => (
  <div style={{
    border: '1px solid var(--border)',
    borderRadius: 8,
    padding: 12,
    minWidth: 150,
    backgroundColor: s.isLow ? '#fff1f0' : 'var(--surface)',
    borderColor: s.isLow ? '#ffa39e' : 'var(--border)'
  }}>
    <div style={{ fontWeight: 600 }}>{s.studentName}</div>
    <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
      ID: <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{s.enrollmentId || s.enrollmentNumber || s.username || s.studentId || 'N/A'}</span>
    </div>
    <div style={{
      fontSize: '1.25rem',
      fontWeight: 'bold',
      color: s.isLow ? '#cf1322' : 'var(--primary)',
      marginTop: 8
    }}>
      {Number(s.percentage).toFixed(1)}%
    </div>
    {(s.present !== undefined) && <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{s.present}/{s.total} present · LATE counts as absent</div>}
    {s.isLow && <div style={{ fontSize: '0.75rem', color: '#cf1322', marginTop: 4, fontWeight: 500 }}>⚠️ Low Attendance</div>}
  </div>
));

const BulkAttendanceRow = React.memo(({ student, onStatus, onRemarks }) => (
  <tr style={{ background: student.alreadyMarked ? '#f8fafc' : 'transparent' }}>
    <td>{student.name} <span style={{ color: '#94a3b8', fontSize: '0.7rem' }}>({student.username || student.enrollmentId || 'N/A'})</span>
      {student.alreadyMarked && <span style={{ marginLeft: 6, fontSize: '0.65rem', background: '#e0f2fe', color: '#075985', padding: '1px 6px', borderRadius: 10 }}>saved</span>}
    </td>
    <td>
      <select
        value={student.status}
        onChange={(e) => onStatus(student.id, e.target.value)}
        style={{ padding: '2px', fontSize: '0.8rem' }}
      >
        {STATUS_OPTIONS.map(o => <option key={o} value={o}>{o.charAt(0) + o.slice(1).toLowerCase()}</option>)}
      </select>
    </td>
    <td>
      <input
        type="text"
        placeholder="optional"
        value={student.remarks || ''}
        onChange={(e) => onRemarks(student.id, e.target.value)}
        maxLength={255}
        style={{ padding: '2px 6px', fontSize: '0.8rem', width: 130 }}
      />
    </td>
    <td style={{ fontSize: '1rem' }}>{student.status === 'PRESENT' ? '✅' : student.status === 'LATE' ? '⏰' : '❌'}</td>
  </tr>
));

export default AttendancePage;

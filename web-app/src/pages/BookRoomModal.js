import React, { useState } from 'react';
import Modal from '../components/Modal';
import { saveTimetableEntry } from '../services/timetableService';

const DEPT_KEY = 'roomBook.dept';
const SEM_KEY = 'roomBook.semester';

const readStored = (key, fallback) => {
    try {
        return localStorage.getItem(key) || fallback;
    } catch {
        return fallback;
    }
};

/**
 * BookRoomModal — books an available room slot by creating a timetable entry.
 * Room + day + slot arrive prefilled from the availability context; the user
 * only fills in the class details.
 */
const BookRoomModal = ({ booking, onClose, onBooked }) => {
    const [form, setForm] = useState({
        department: readStored(DEPT_KEY, ''),
        semester: readStored(SEM_KEY, ''),
        subject: '',
        facultyName: '',
        specialization: ''
    });
    const [saving, setSaving] = useState(false);
    const [modalError, setModalError] = useState('');

    if (!booking) return null;

    const handleChange = (e) => {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setModalError('');
    };

    const handleSubmit = async () => {
        if (!form.department.trim() || !form.semester || !form.subject.trim()) {
            setModalError('Department, semester, and subject are required.');
            return;
        }
        const semNum = Number(form.semester);
        if (!Number.isInteger(semNum) || semNum < 1) {
            setModalError('Semester must be a positive number.');
            return;
        }
        setSaving(true);
        setModalError('');
        try {
            await saveTimetableEntry({
                department: form.department.trim(),
                semester: semNum,
                dayOfWeek: booking.day,
                timeSlot: booking.timeSlot,
                subject: form.subject.trim(),
                facultyName: form.facultyName.trim(),
                roomNumber: booking.roomNumber,
                specialization: form.specialization.trim(),
                courseId: 0
            });
            try {
                localStorage.setItem(DEPT_KEY, form.department.trim());
                localStorage.setItem(SEM_KEY, String(semNum));
            } catch {
                // Non-fatal: prefill just won't persist.
            }
            onBooked();
        } catch (err) {
            const status = err.response?.status;
            const msg = err.response?.data?.error || 'Failed to book this slot.';
            setModalError(status === 409
                ? `Conflict: ${msg} Please pick another slot.`
                : status === 403
                    ? 'Forbidden: your account needs the BOOK_ROOM permission to book rooms.'
                    : msg);
        } finally {
            setSaving(false);
        }
    };

    return (
        <Modal
            isOpen={!!booking}
            title={`📅 Book ${booking.roomNumber}`}
            onClose={onClose}
            onSubmit={handleSubmit}
            submitLabel="Confirm Booking"
            submitting={saving}
        >
            <form onSubmit={e => e.preventDefault()}>
                <p className="text-muted" style={{ marginBottom: '16px' }}>
                    {booking.day} · {booking.timeSlot}
                </p>
                {modalError && <div className="alert alert-error" style={{ marginBottom: '14px' }}>{modalError}</div>}
                <div className="form-grid">
                    <div className="form-group">
                        <label className="form-label" htmlFor="book-dept">Department *</label>
                        <input id="book-dept" name="department" className="form-control" type="text"
                            required placeholder="e.g. Computer Science"
                            value={form.department} onChange={handleChange} />
                    </div>
                    <div className="form-group">
                        <label className="form-label" htmlFor="book-sem">Semester *</label>
                        <input id="book-sem" name="semester" className="form-control" type="number"
                            required min="1" max="12" placeholder="e.g. 3"
                            value={form.semester} onChange={handleChange} />
                    </div>
                    <div className="form-group form-span-2">
                        <label className="form-label" htmlFor="book-subject">Subject *</label>
                        <input id="book-subject" name="subject" className="form-control" type="text"
                            required placeholder="e.g. Data Structures"
                            value={form.subject} onChange={handleChange} />
                    </div>
                    <div className="form-group">
                        <label className="form-label" htmlFor="book-faculty">Faculty</label>
                        <input id="book-faculty" name="facultyName" className="form-control" type="text"
                            placeholder="Faculty name (optional)"
                            value={form.facultyName} onChange={handleChange} />
                    </div>
                    <div className="form-group">
                        <label className="form-label" htmlFor="book-track">Track / Specialization</label>
                        <input id="book-track" name="specialization" className="form-control" type="text"
                            placeholder="Optional"
                            value={form.specialization} onChange={handleChange} />
                    </div>
                </div>
            </form>
        </Modal>
    );
};

export default BookRoomModal;

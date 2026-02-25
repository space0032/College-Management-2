import React, { useState, useEffect } from 'react';
import {
    getAllGrades, getStudentGrades, getStudentCGPA, saveGrade
} from '../services/gradeService';
import { getAllCourses } from '../services/courseService';
import { getAllStudents } from '../services/studentService';

const GradesPage = () => {
    const [activeTab, setActiveTab] = useState('view');

    // View State
    const [grades, setGrades] = useState([]);
    const [cgpa, setCgpa] = useState(null);

    // Manage State (Admin/Faculty)
    const [students, setStudents] = useState([]);
    const [courses, setCourses] = useState([]);
    const [formData, setFormData] = useState({
        studentId: '',
        courseId: '',
        examType: 'MID TERM',
        marksObtained: '',
        grade: 'A'
    });

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: 2, role: 'STUDENT' };

    useEffect(() => {
        if (activeTab === 'view') {
            if (user.role === 'STUDENT') {
                loadStudentGrades(user.id);
            } else {
                loadAllGrades();
            }
        } else if (activeTab === 'manage') {
            loadFormData();
        }
        // eslint-disable-next-line
    }, [activeTab, user.id, user.role]);

    const loadStudentGrades = async (studentId) => {
        try {
            const res = await getStudentGrades(studentId);
            setGrades(res.data || []);
            const cgpaRes = await getStudentCGPA(studentId);
            setCgpa(cgpaRes.data?.cgpa);
        } catch (err) {
            console.error(err);
        }
    };

    const loadAllGrades = async () => {
        try {
            const res = await getAllGrades();
            setGrades(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const loadFormData = async () => {
        try {
            const pStudents = getAllStudents();
            const pCourses = getAllCourses();
            const [rStud, rCour] = await Promise.all([pStudents, pCourses]);
            setStudents(rStud.data || []);
            setCourses(rCour.data || []);
        } catch (err) {
            console.error('Failed to load form data', err);
        }
    };

    const handleSaveGrade = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                studentId: parseInt(formData.studentId),
                courseId: parseInt(formData.courseId),
                examType: formData.examType,
                marksObtained: parseFloat(formData.marksObtained),
                grade: formData.grade
            };
            await saveGrade(payload);
            alert('Grade saved successfully!');
            setFormData({ ...formData, marksObtained: '', grade: 'A' });
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to save grade');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Grades & Transcripts</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'view' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('view')}
                    >
                        {user.role === 'STUDENT' ? 'My Grades' : 'All Grades'}
                    </button>
                    {(user.role === 'ADMIN' || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Enter/Edit Grades
                        </button>
                    )}
                </div>
            </div>

            {activeTab === 'view' && (
                <>
                    {user.role === 'STUDENT' && cgpa !== null && (
                        <div className="stat-card" style={{ marginBottom: '20px', background: 'linear-gradient(135deg, #2a0845 0%, #6441A5 100%)', color: 'white' }}>
                            <h3>Cumulative Grade Point Average (CGPA)</h3>
                            <h1 style={{ fontSize: '3rem', margin: '10px 0' }}>{cgpa.toFixed(2)}</h1>
                            <p>Scale: 10.0</p>
                        </div>
                    )}

                    <div className="data-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    {user.role !== 'STUDENT' && <th>Student Name</th>}
                                    {user.role !== 'STUDENT' && <th>Enrollment No</th>}
                                    <th>Course Name</th>
                                    <th>Credits</th>
                                    <th>Exam Type</th>
                                    <th>Marks (%)</th>
                                    <th>Letter Grade</th>
                                </tr>
                            </thead>
                            <tbody>
                                {grades.length === 0 ? (
                                    <tr>
                                        <td colSpan={user.role !== 'STUDENT' ? 7 : 5} style={{ textAlign: 'center' }}>No grades found.</td>
                                    </tr>
                                ) : (
                                    grades.map(g => (
                                        <tr key={g.id}>
                                            {user.role !== 'STUDENT' && <td>{g.studentName}</td>}
                                            {user.role !== 'STUDENT' && <td>{g.enrollmentNumber || 'N/A'}</td>}
                                            <td>{g.courseName}</td>
                                            <td>{g.credits}</td>
                                            <td>{g.examType}</td>
                                            <td>{g.marksObtained}</td>
                                            <td>
                                                <span className={`badge ${g.grade === 'F' ? 'badge-danger' : (['A', 'B'].includes(g.grade) ? 'badge-success' : 'badge-warning')}`}>
                                                    {g.grade}
                                                </span>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}

            {activeTab === 'manage' && (
                <div className="stat-card">
                    <h3>Record Student Grade</h3>
                    <form className="form-grid" onSubmit={handleSaveGrade} style={{ marginTop: '20px' }}>
                        <div className="form-group">
                            <label>Student *</label>
                            <select
                                required
                                value={formData.studentId}
                                onChange={e => setFormData({ ...formData, studentId: e.target.value })}
                            >
                                <option value="">-- Select Student --</option>
                                {students.map(s => <option key={s.id} value={s.id}>{s.name} ({s.enrollmentNumber})</option>)}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Course *</label>
                            <select
                                required
                                value={formData.courseId}
                                onChange={e => setFormData({ ...formData, courseId: e.target.value })}
                            >
                                <option value="">-- Select Course --</option>
                                {courses.map(c => <option key={c.id} value={c.id}>{c.name} ({c.code})</option>)}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Exam Type *</label>
                            <select
                                required
                                value={formData.examType}
                                onChange={e => setFormData({ ...formData, examType: e.target.value })}
                            >
                                <option value="MID TERM">Mid Term</option>
                                <option value="END TERM">End Term</option>
                                <option value="ASSIGNMENT">Assignment</option>
                                <option value="PRACTICAL">Practical</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Marks Obtained (%) *</label>
                            <input
                                type="number"
                                step="0.1"
                                min="0"
                                max="100"
                                required
                                value={formData.marksObtained}
                                onChange={e => setFormData({ ...formData, marksObtained: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label>Letter Grade *</label>
                            <select
                                required
                                value={formData.grade}
                                onChange={e => setFormData({ ...formData, grade: e.target.value })}
                            >
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="D">D</option>
                                <option value="E">E</option>
                                <option value="F">F</option>
                            </select>
                        </div>

                        <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
                            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Save Grade</button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
};

export default GradesPage;

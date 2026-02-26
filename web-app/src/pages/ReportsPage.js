import React, { useState, useEffect, useCallback } from 'react';
import { generateVisitorReportPdf, getPlacementStats } from '../services/reportService';
import { getAllFees } from '../services/feesService';
import { getAllGrades } from '../services/gradeService';
import { getCourseStats } from '../services/attendanceService';
import { exportToCSV } from '../utils/exportUtils';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, AreaChart, Area, PieChart, Pie, Cell } from 'recharts';

const GRADE_COLORS = { A: '#10b981', B: '#3b82f6', C: '#f59e0b', D: '#f97316', E: '#ef4444', F: '#dc2626' };

const ReportsPage = () => {
    const [activeTab, setActiveTab] = useState('attendance');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [pdfMessage, setPdfMessage] = useState('');
    const [isGenerating, setIsGenerating] = useState(false);
    const [placementStats, setPlacementStats] = useState(null);

    const [attendanceStats, setAttendanceStats] = useState(null);
    const [attendanceLoading, setAttendanceLoading] = useState(false);
    const [courseIdInput, setCourseIdInput] = useState('');
    const [feesSummary, setFeesSummary] = useState({ totalRevenue: 0, collected: 0, pending: 0, feeByType: [] });
    const [feesLoading, setFeesLoading] = useState(false);
    const [gradeData, setGradeData] = useState([]);
    const [gradesLoading, setGradesLoading] = useState(false);

    useEffect(() => {
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
        const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];
        setStartDate(firstDay);
        setEndDate(lastDay);
    }, []);

    useEffect(() => {
        if (activeTab === 'placements') loadPlacementStats();
        if (activeTab === 'fees') loadFeesSummary();
        if (activeTab === 'grades') loadGradeDistribution();
    }, [activeTab]); // eslint-disable-line react-hooks/exhaustive-deps

    const loadPlacementStats = async () => {
        try { const res = await getPlacementStats(); setPlacementStats(res.data); }
        catch (err) { console.error(err); }
    };

    const loadFeesSummary = async () => {
        setFeesLoading(true);
        try {
            const res = await getAllFees();
            const fees = res.data || [];
            const totalRevenue = fees.reduce((s, f) => s + (f.totalAmount || 0), 0);
            const collected = fees.filter(f => f.status === 'PAID').reduce((s, f) => s + (f.paidAmount || f.totalAmount || 0), 0);
            const pending = totalRevenue - collected;
            const byType = fees.reduce((acc, f) => {
                const t = f.feeType || f.categoryName || 'Other';
                acc[t] = (acc[t] || 0) + (f.totalAmount || 0);
                return acc;
            }, {});
            setFeesSummary({ totalRevenue, collected, pending, feeByType: Object.entries(byType).map(([name, value]) => ({ name, value })) });
        } catch (err) { console.error(err); }
        finally { setFeesLoading(false); }
    };

    const loadGradeDistribution = async () => {
        setGradesLoading(true);
        try {
            const res = await getAllGrades();
            const dist = (res.data || []).reduce((acc, g) => { const l = g.grade || 'N/A'; acc[l] = (acc[l] || 0) + 1; return acc; }, {});
            const sorted = ['A', 'B', 'C', 'D', 'E', 'F'].map(g => ({ grade: g, students: dist[g] || 0 })).filter(x => x.students > 0);
            setGradeData(sorted);
        } catch (err) { console.error(err); }
        finally { setGradesLoading(false); }
    };

    const loadAttendanceStats = useCallback(async () => {
        if (!courseIdInput.trim()) return;
        setAttendanceLoading(true);
        try {
            const res = await getCourseStats(courseIdInput.trim());
            setAttendanceStats(res.data?.stats || res.data || null);
        } catch (err) { console.error(err); setAttendanceStats(null); }
        finally { setAttendanceLoading(false); }
    }, [courseIdInput]);

    const handleGeneratePdf = async (e) => {
        e.preventDefault(); setIsGenerating(true); setPdfMessage('Initiating PDF engine...');
        try {
            const res = await generateVisitorReportPdf(startDate, endDate);
            setPdfMessage(`✅ Success: ${res.data.count} records. File sent to system.`);
        } catch (err) { setPdfMessage('❌ ' + (err.response?.data?.error || 'Failed.')); }
        finally { setIsGenerating(false); }
    };

    const setDateRange = (days) => {
        const end = new Date(), start = new Date();
        start.setDate(end.getDate() - days);
        setStartDate(start.toISOString().split('T')[0]);
        setEndDate(end.toISOString().split('T')[0]);
    };

    const PIE_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#f43f5e'];

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">📊 Intelligence &amp; Reports</h1>
                    <p className="page-subtitle">Extract insights, generate regulatory PDFs, and monitor campus performance</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', background: '#f1f5f9', padding: '4px', borderRadius: '8px', overflowX: 'auto' }}>
                    {['attendance', 'fees', 'grades', 'visitors', 'placements'].map(t => (
                        <button key={t} className={`btn btn-sm ${activeTab === t ? 'btn-primary' : ''}`}
                            style={activeTab !== t ? { background: 'transparent', border: 'none', color: '#64748b', whiteSpace: 'nowrap' } : { whiteSpace: 'nowrap' }}
                            onClick={() => setActiveTab(t)}>
                            {t.charAt(0).toUpperCase() + t.slice(1)}
                        </button>
                    ))}
                </div>
            </div>

            {/* ATTENDANCE */}
            {activeTab === 'attendance' && (
                <div>
                    <div className="stat-card" style={{ marginBottom: 20 }}>
                        <h3 style={{ margin: '0 0 16px' }}>📈 Attendance Analytics</h3>
                        <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
                            <div className="form-group" style={{ margin: 0, flex: 1 }}>
                                <label className="form-label">Course ID</label>
                                <input className="form-control" placeholder="e.g. CS101" value={courseIdInput}
                                    onChange={e => setCourseIdInput(e.target.value)}
                                    onKeyDown={e => e.key === 'Enter' && loadAttendanceStats()} />
                            </div>
                            <button className="btn btn-primary" onClick={loadAttendanceStats} disabled={attendanceLoading}>
                                {attendanceLoading ? '⌛ Loading...' : '🔍 Load Stats'}
                            </button>
                            {attendanceStats && Array.isArray(attendanceStats) && (
                                <button className="btn btn-secondary" onClick={() => exportToCSV(
                                    ['Student ID', 'Present', 'Absent', 'Percentage'],
                                    attendanceStats.map(s => [s.studentId, s.presentCount, s.absentCount, (s.percentage || 0) + '%']),
                                    `attendance_report_${courseIdInput}`
                                )}>⬇ Export CSV</button>
                            )}
                        </div>
                    </div>
                    {attendanceStats && Array.isArray(attendanceStats) && (
                        <div className="stat-card">
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16, marginBottom: 20 }}>
                                {[
                                    { label: 'Total Students', value: attendanceStats.length, color: '#3b82f6' },
                                    { label: 'Avg. Attendance', value: (attendanceStats.reduce((s, x) => s + (parseFloat(x.percentage) || 0), 0) / (attendanceStats.length || 1)).toFixed(1) + '%', color: '#10b981' },
                                    { label: 'Below 75%', value: attendanceStats.filter(x => parseFloat(x.percentage) < 75).length, color: '#ef4444' },
                                ].map(s => (
                                    <div key={s.label} style={{ background: '#f8fafc', borderRadius: 12, padding: '16px', textAlign: 'center' }}>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>{s.label}</div>
                                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: s.color }}>{s.value}</div>
                                    </div>
                                ))}
                            </div>
                            <ResponsiveContainer width="100%" height={250}>
                                <BarChart data={attendanceStats.slice(0, 20)} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                    <XAxis dataKey="studentId" tick={{ fontSize: 11 }} />
                                    <YAxis domain={[0, 100]} unit="%" />
                                    <Tooltip formatter={v => v + '%'} />
                                    <Bar dataKey="percentage" name="Attendance %" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    )}
                    {!attendanceStats && !attendanceLoading && (
                        <div className="stat-card" style={{ textAlign: 'center', padding: '60px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem', marginBottom: 12 }}>📋</div>
                            <p>Enter a course ID above and click "Load Stats" to view real attendance data</p>
                        </div>
                    )}
                </div>
            )}

            {/* FEES */}
            {activeTab === 'fees' && (
                <div>
                    {feesLoading ? (
                        <div className="loading-container"><div className="spinner" /><span>Loading fee data...</span></div>
                    ) : (
                        <>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 20, marginBottom: 20 }}>
                                {[
                                    { label: 'Total Billed', value: '₹' + feesSummary.totalRevenue.toLocaleString('en-IN'), color: '#3b82f6' },
                                    { label: 'Collected', value: '₹' + feesSummary.collected.toLocaleString('en-IN'), color: '#10b981' },
                                    { label: 'Pending', value: '₹' + feesSummary.pending.toLocaleString('en-IN'), color: '#ef4444' },
                                ].map(s => (
                                    <div key={s.label} className="stat-card" style={{ textAlign: 'center' }}>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>{s.label}</div>
                                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: s.color }}>{s.value}</div>
                                    </div>
                                ))}
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                                <div className="stat-card">
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                                        <h3 style={{ margin: 0 }}>💰 Revenue by Fee Type</h3>
                                        <button className="btn btn-sm btn-secondary" onClick={() => exportToCSV(['Fee Type', 'Amount'], feesSummary.feeByType.map(f => [f.name, f.value]), 'fee_summary')}>⬇ Export CSV</button>
                                    </div>
                                    <ResponsiveContainer width="100%" height={220}>
                                        <PieChart>
                                            <Pie data={feesSummary.feeByType} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                                                {feesSummary.feeByType.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                                            </Pie>
                                            <Tooltip formatter={v => '₹' + v.toLocaleString('en-IN')} />
                                        </PieChart>
                                    </ResponsiveContainer>
                                </div>
                                <div className="stat-card">
                                    <h3 style={{ margin: '0 0 16px' }}>📊 Collection Status</h3>
                                    <ResponsiveContainer width="100%" height={220}>
                                        <BarChart data={[{ name: 'Collected', value: feesSummary.collected }, { name: 'Pending', value: feesSummary.pending }]} barSize={60}>
                                            <XAxis dataKey="name" />
                                            <YAxis tickFormatter={v => '₹' + (v / 1000).toFixed(0) + 'k'} />
                                            <Tooltip formatter={v => '₹' + v.toLocaleString('en-IN')} />
                                            <Bar dataKey="value" name="Amount">
                                                <Cell fill="#10b981" />
                                                <Cell fill="#ef4444" />
                                            </Bar>
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            )}

            {/* GRADES */}
            {activeTab === 'grades' && (
                <div className="stat-card">
                    <h3 style={{ margin: '0 0 16px' }}>🎓 Academic Grade Distribution</h3>
                    {gradesLoading ? (
                        <div className="loading-container"><div className="spinner" /><span>Computing grade curve...</span></div>
                    ) : gradeData.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '60px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem', marginBottom: 12 }}>📚</div>
                            <p>No grade data available. Enter grades via the Grades page first.</p>
                        </div>
                    ) : (
                        <>
                            <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap' }}>
                                {gradeData.map(g => (
                                    <div key={g.grade} style={{ background: (GRADE_COLORS[g.grade] || '#6366f1') + '18', border: `1px solid ${GRADE_COLORS[g.grade] || '#6366f1'}`, borderRadius: 8, padding: '8px 16px', textAlign: 'center' }}>
                                        <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: GRADE_COLORS[g.grade] || '#6366f1' }}>{g.grade}</div>
                                        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{g.students} students</div>
                                    </div>
                                ))}
                            </div>
                            <ResponsiveContainer width="100%" height={280}>
                                <AreaChart data={gradeData} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                    <XAxis dataKey="grade" axisLine={false} tickLine={false} />
                                    <YAxis axisLine={false} tickLine={false} />
                                    <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                                    <Area type="monotone" dataKey="students" stroke="#3b82f6" fill="#bfdbfe" strokeWidth={3} name="Students" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </>
                    )}
                </div>
            )}

            {/* VISITORS */}
            {activeTab === 'visitors' && (
                <div style={{ display: 'flex', gap: '40px', alignItems: 'flex-start' }}>
                    <div className="stat-card" style={{ flex: 1, padding: '30px' }}>
                        <h3 style={{ margin: '0 0 10px' }}>Visitor Registry Export</h3>
                        <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '25px' }}>Generate an audited PDF document of all campus visitors within a specific timeframe for security compliance.</p>
                        <div style={{ display: 'flex', gap: '10px', marginBottom: '25px' }}>
                            <button className="btn btn-sm" onClick={() => setDateRange(0)} style={{ background: '#f1f5f9', border: 'none' }}>Today</button>
                            <button className="btn btn-sm" onClick={() => setDateRange(7)} style={{ background: '#f1f5f9', border: 'none' }}>Last 7 Days</button>
                            <button className="btn btn-sm" onClick={() => setDateRange(30)} style={{ background: '#f1f5f9', border: 'none' }}>Last 30 Days</button>
                        </div>
                        <form className="form-grid" onSubmit={handleGeneratePdf}>
                            <div className="form-group">
                                <label>Start Date</label>
                                <input type="date" required className="form-control" value={startDate} onChange={e => setStartDate(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label>End Date</label>
                                <input type="date" required className="form-control" value={endDate} onChange={e => setEndDate(e.target.value)} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '15px' }} disabled={isGenerating}>
                                    {isGenerating ? '💫 Synthesizing PDF...' : '💾 Generate Official PDF'}
                                </button>
                            </div>
                        </form>
                        {pdfMessage && (
                            <div style={{ marginTop: '25px', padding: '15px', borderRadius: '8px', fontSize: '0.9rem', background: pdfMessage.includes('✅') ? '#f0fff4' : '#fff5f5', color: pdfMessage.includes('✅') ? '#2f855a' : '#c53030', border: `1px solid ${pdfMessage.includes('✅') ? '#c6f6d5' : '#feb2b2'}` }}>
                                {pdfMessage}
                            </div>
                        )}
                    </div>
                    <div className="stat-card" style={{ flex: 0.7, background: '#f8fafc', borderStyle: 'dashed' }}>
                        <h4>Report Preview</h4>
                        <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '4px', padding: '20px', marginTop: '15px', minHeight: '300px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            <div style={{ height: '15px', width: '60%', background: '#f1f5f9' }} />
                            <div style={{ height: '30px', width: '100%', background: '#f8fafc', marginTop: '10px' }} />
                            <div style={{ height: '80px', width: '100%', background: '#f1f5f9' }} />
                            <div style={{ height: '15px', width: '40%', background: '#f1f5f9' }} />
                        </div>
                        <p style={{ fontSize: '0.8rem', color: '#94a3b8', textAlign: 'center', marginTop: '15px' }}>Final document will contain QR signatures and institutional headers.</p>
                    </div>
                </div>
            )}

            {/* PLACEMENTS — no Math.random() */}
            {activeTab === 'placements' && !placementStats && (
                <div className="loading-container"><div className="spinner" /><span>Loading placement data...</span></div>
            )}
            {activeTab === 'placements' && placementStats && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '25px' }}>
                        {[
                            { label: 'Total Drives', value: placementStats.totalDrives, color: '#6366f1' },
                            { label: 'Active Drives', value: placementStats.activeDrives, color: '#10b981' },
                            { label: 'Total Applications', value: placementStats.totalApplications ?? '—', color: '#f59e0b' },
                        ].map(s => (
                            <div key={s.label} className="stat-card" style={{ borderTop: `4px solid ${s.color}` }}>
                                <div style={{ fontSize: '0.9rem', color: '#64748b' }}>{s.label}</div>
                                <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: s.color, margin: '10px 0' }}>{s.value}</div>
                            </div>
                        ))}
                    </div>
                    {placementStats.companySummary?.length > 0 && (
                        <div className="stat-card">
                            <h4 style={{ marginBottom: '20px' }}>Recruiting Companies</h4>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '15px' }}>
                                {placementStats.companySummary.map(co => (
                                    <div key={co.company} style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center', border: '1px solid #f1f5f9' }}>
                                        <div style={{ fontSize: '1rem', fontWeight: 'bold', color: '#475569' }}>{co.company}</div>
                                        <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '5px' }}>{co.offers || co.applications} Offers</div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default ReportsPage;

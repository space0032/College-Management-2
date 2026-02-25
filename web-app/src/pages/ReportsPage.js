import React, { useState, useEffect } from 'react';
import { generateVisitorReportPdf, getPlacementStats } from '../services/reportService';
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, AreaChart, Area } from 'recharts';

const ReportsPage = () => {
    const [activeTab, setActiveTab] = useState('attendance'); // attendance, fees, grades, visitors, placements
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [pdfMessage, setPdfMessage] = useState('');
    const [isGenerating, setIsGenerating] = useState(false);
    const [placementStats, setPlacementStats] = useState(null);

    useEffect(() => {
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
        const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];
        setStartDate(firstDay);
        setEndDate(lastDay);

        if (activeTab === 'placements') loadPlacementStats();
    }, [activeTab]);

    const loadPlacementStats = async () => {
        try {
            const res = await getPlacementStats();
            setPlacementStats(res.data);
        } catch (err) { console.error(err); }
    };

    const handleGeneratePdf = async (e) => {
        e.preventDefault();
        setIsGenerating(true);
        setPdfMessage('Initiating PDF engine...');
        try {
            const res = await generateVisitorReportPdf(startDate, endDate);
            setPdfMessage(`✅ Success: ${res.data.count} records processed. File sent to printer/system.`);
        } catch (err) {
            setPdfMessage('❌ Error: ' + (err.response?.data?.error || 'Failed to generate report.'));
        } finally {
            setIsGenerating(false);
        }
    };

    const setDateRange = (days) => {
        const end = new Date();
        const start = new Date();
        start.setDate(end.getDate() - days);
        setStartDate(start.toISOString().split('T')[0]);
        setEndDate(end.toISOString().split('T')[0]);
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">📊 Intelligence & Reports</h1>
                    <p className="page-subtitle">Extract insights, generate regulatory PDFs, and monitor campus performance</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', background: '#f1f5f9', padding: '4px', borderRadius: '8px', overflowX: 'auto' }}>
                    {['attendance', 'fees', 'grades', 'visitors', 'placements'].map(t => (
                        <button
                            key={t}
                            className={`btn btn-sm ${activeTab === t ? 'btn-primary' : ''}`}
                            style={activeTab !== t ? { background: 'transparent', border: 'none', color: '#64748b', whiteSpace: 'nowrap' } : { whiteSpace: 'nowrap' }}
                            onClick={() => setActiveTab(t)}
                        >
                            {t.charAt(0).toUpperCase() + t.slice(1)}
                        </button>
                    ))}
                </div>
            </div>

            {activeTab === 'attendance' && (
                <div className="stat-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3>📈 Attendance Analytics</h3>
                        <button className="btn btn-sm btn-secondary">⬇ Download CSV</button>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
                        <div style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center' }}>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Avg. Daily Percentage</div>
                            <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>92.4%</div>
                        </div>
                        <div style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center' }}>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Absenteeism Rate</div>
                            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ef4444' }}>7.6%</div>
                        </div>
                        <div style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center' }}>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Consistent Performers</div>
                            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#10b981' }}>842</div>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'fees' && (
                <div className="stat-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3>💰 Finance Intelligence</h3>
                        <button className="btn btn-sm btn-secondary">⬇ Revenue Summary</button>
                    </div>
                    <div style={{ height: '300px', width: '100%', padding: '20px 0' }}>
                        <ResponsiveContainer>
                            <BarChart data={[
                                { week: 'Week 1', revenue: 65000 },
                                { week: 'Week 2', revenue: 45000 },
                                { week: 'Week 3', revenue: 85000 },
                                { week: 'Week 4', revenue: 70000 },
                                { week: 'Week 5', revenue: 95000 },
                                { week: 'Week 6', revenue: 60000 },
                                { week: 'Week 7', revenue: 40000 }
                            ]} margin={{ top: 20, right: 30, left: 10, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                <XAxis dataKey="week" axisLine={false} tickLine={false} />
                                <YAxis axisLine={false} tickLine={false} tickFormatter={(value) => `$${value / 1000}k`} />
                                <Tooltip cursor={{ fill: '#f8fafc' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} formatter={(value) => `$${value.toLocaleString()}`} />
                                <Bar dataKey="revenue" fill="#8b5cf6" radius={[4, 4, 0, 0]} barSize={40} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                    <p style={{ textAlign: 'center', fontSize: '0.8rem', color: '#94a3b8' }}>Weekly Revenue Collection (Simulated)</p>
                </div>
            )}

            {activeTab === 'grades' && (
                <div className="stat-card">
                    <h3>🎓 Academic Grade Curve</h3>
                    <p className="text-muted">Distribution of grades across the current semester.</p>
                    <div style={{ height: '300px', width: '100%', marginTop: '20px' }}>
                        <ResponsiveContainer>
                            <AreaChart data={[
                                { grade: 'A+', students: 12 },
                                { grade: 'A', students: 45 },
                                { grade: 'B+', students: 68 },
                                { grade: 'B', students: 110 },
                                { grade: 'C', students: 85 },
                                { grade: 'D', students: 30 },
                                { grade: 'F', students: 15 }
                            ]} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                <XAxis dataKey="grade" axisLine={false} tickLine={false} />
                                <YAxis axisLine={false} tickLine={false} />
                                <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                                <Area type="monotone" dataKey="students" stroke="#3b82f6" fill="#bfdbfe" strokeWidth={3} />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            )}

            {activeTab === 'visitors' && (
                <div style={{ display: 'flex', gap: '40px', alignItems: 'flex-start' }}>
                    <div className="stat-card" style={{ flex: 1, padding: '30px' }}>
                        <h3 style={{ margin: '0 0 10px 0' }}>Visitor Registry Export</h3>
                        <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '25px' }}>
                            Generate an audited PDF document of all campus visitors within a specific timeframe for security compliance.
                        </p>

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
                            <div style={{
                                marginTop: '25px', padding: '15px', borderRadius: '8px', fontSize: '0.9rem',
                                background: pdfMessage.includes('✅') ? '#f0fff4' : '#fff5f5',
                                color: pdfMessage.includes('✅') ? '#2f855a' : '#c53030',
                                border: `1px solid ${pdfMessage.includes('✅') ? '#c6f6d5' : '#feb2b2'}`
                            }}>
                                {pdfMessage}
                            </div>
                        )}
                    </div>

                    <div className="stat-card" style={{ flex: 0.7, background: '#f8fafc', borderStyle: 'dashed' }}>
                        <h4>Report Preview</h4>
                        <div style={{
                            background: 'white', border: '1px solid #e2e8f0', borderRadius: '4px', padding: '20px',
                            marginTop: '15px', minHeight: '300px', display: 'flex', flexDirection: 'column', gap: '10px'
                        }}>
                            <div style={{ height: '15px', width: '60%', background: '#f1f5f9' }} />
                            <div style={{ height: '30px', width: '100%', background: '#f8fafc', marginTop: '10px' }} />
                            <div style={{ height: '80px', width: '100%', background: '#f1f5f9' }} />
                            <div style={{ height: '15px', width: '40%', background: '#f1f5f9' }} />
                            <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'space-between' }}>
                                <div style={{ height: '10px', width: '20%', background: '#e2e8f0' }} />
                                <div style={{ height: '10px', width: '10%', background: '#e2e8f0' }} />
                            </div>
                        </div>
                        <p style={{ fontSize: '0.8rem', color: '#94a3b8', textAlign: 'center', marginTop: '15px' }}>
                            Final document will contain QR signatures and institutional headers.
                        </p>
                    </div>
                </div>
            )}

            {activeTab === 'placements' && placementStats && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '25px' }}>
                        <div className="stat-card" style={{ borderTop: '4px solid #6366f1' }}>
                            <div style={{ fontSize: '0.9rem', color: '#64748b' }}>Total Drives</div>
                            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#1e293b', margin: '10px 0' }}>{placementStats.totalDrives}</div>
                            <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Cumulative across all sessions</p>
                        </div>
                        <div className="stat-card" style={{ borderTop: '4px solid #10b981' }}>
                            <div style={{ fontSize: '0.9rem', color: '#64748b' }}>Currently Active</div>
                            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#10b981', margin: '10px 0' }}>{placementStats.activeDrives}</div>
                            <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Companies currently on campus</p>
                        </div>
                        <div className="stat-card" style={{ borderTop: '4px solid #f59e0b' }}>
                            <div style={{ fontSize: '0.9rem', color: '#64748b' }}>Avg. Package (LPA)</div>
                            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#f59e0b', margin: '10px 0' }}>8.5</div>
                            <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Average annual CTC offered</p>
                        </div>
                    </div>

                    <div className="stat-card">
                        <h4 style={{ marginBottom: '20px' }}>Top Recruiting Partners (Simulated)</h4>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '15px' }}>
                            {['Google', 'Microsoft', 'Amazon', 'TCS', 'Infosys'].map(co => (
                                <div key={co} style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center', border: '1px solid #f1f5f9' }}>
                                    <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: '#475569' }}>{co}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '5px' }}>{Math.floor(Math.random() * 20) + 5} Offers</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReportsPage;

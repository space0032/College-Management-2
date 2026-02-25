import React, { useState, useEffect } from 'react';
import { generateVisitorReportPdf, getPlacementStats } from '../services/reportService';

const ReportsPage = () => {
    const [activeTab, setActiveTab] = useState('analytics'); // analytics, visitors, placements
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
                <div style={{ display: 'flex', gap: '8px', background: '#f1f5f9', padding: '4px', borderRadius: '8px' }}>
                    <button
                        className={`btn btn-sm ${activeTab === 'analytics' ? 'btn-primary' : ''}`}
                        style={activeTab !== 'analytics' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}}
                        onClick={() => setActiveTab('analytics')}
                    >
                        Dashboard
                    </button>
                    <button
                        className={`btn btn-sm ${activeTab === 'visitors' ? 'btn-primary' : ''}`}
                        style={activeTab !== 'visitors' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}}
                        onClick={() => setActiveTab('visitors')}
                    >
                        Export PDF
                    </button>
                    <button
                        className={`btn btn-sm ${activeTab === 'placements' ? 'btn-primary' : ''}`}
                        style={activeTab !== 'placements' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}}
                        onClick={() => setActiveTab('placements')}
                    >
                        Placements
                    </button>
                </div>
            </div>

            {activeTab === 'analytics' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gridTemplateRows: 'repeat(2, auto)', gap: '25px' }}>
                    {/* Top Row: Quick Stats */}
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)', color: 'white' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ fontSize: '0.8rem', opacity: 0.9 }}>Total Revenue (MTD)</span>
                            <span>📈</span>
                        </div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '10px 0' }}>$142,500</div>
                        <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>+12% vs last month</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                            <span style={{ fontSize: '0.8rem' }}>Course Completion</span>
                            <span>🎓</span>
                        </div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '10px 0', color: '#0f172a' }}>88.4%</div>
                        <div style={{ width: '100%', height: '6px', background: '#f1f5f9', borderRadius: '3px', overflow: 'hidden' }}>
                            <div style={{ width: '88%', height: '100%', background: '#10b981' }} />
                        </div>
                    </div>
                    <div className="stat-card">
                        <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                            <span style={{ fontSize: '0.8rem' }}>System Uptime</span>
                            <span>⚡</span>
                        </div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '10px 0', color: '#0f172a' }}>99.98%</div>
                        <div style={{ fontSize: '0.75rem', color: '#10b981' }}>● All services operational</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                            <span style={{ fontSize: '0.8rem' }}>Avg. Attendance</span>
                            <span>🙋</span>
                        </div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '10px 0', color: '#0f172a' }}>92%</div>
                        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Across 24 departments</div>
                    </div>

                    {/* Middle Row: Detailed Insights */}
                    <div className="stat-card" style={{ gridColumn: '1 / span 2', minHeight: '300px' }}>
                        <h4 style={{ margin: '0 0 20px 0' }}>Department Performance Index</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            {[
                                { name: 'Comp. Science', score: 94, color: '#6366f1' },
                                { name: 'Mechanical', score: 82, color: '#f59e0b' },
                                { name: 'Business Admn', score: 89, color: '#10b981' },
                                { name: 'Electronics', score: 76, color: '#ef4444' }
                            ].map(dept => (
                                <div key={dept.name}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '5px' }}>
                                        <span>{dept.name}</span>
                                        <strong>{dept.score}%</strong>
                                    </div>
                                    <div style={{ height: '8px', background: '#f1f5f9', borderRadius: '4px', overflow: 'hidden' }}>
                                        <div style={{ width: `${dept.score}%`, height: '100%', background: dept.color }} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="stat-card" style={{ gridColumn: '3 / span 2' }}>
                        <h4 style={{ margin: '0 0 15px 0' }}>Recent System Events</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {[
                                { event: 'PDF Report Generated', time: '10 mins ago', user: 'Admin' },
                                { event: 'Bulk Marks Uploaded', time: '1 hour ago', user: 'Prof. Sarah' },
                                { event: 'Hostel Maintenance Split', time: '3 hours ago', user: 'System' },
                                { event: 'New Club Approved', time: '5 hours ago', user: 'Moderator' }
                            ].map((e, i) => (
                                <div key={i} style={{ display: 'flex', gap: '12px', fontSize: '0.85rem', paddingBottom: '12px', borderBottom: i === 3 ? 'none' : '1px solid #f1f5f9' }}>
                                    <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#cbd5e1', alignSelf: 'center' }} />
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: '600' }}>{e.event}</div>
                                        <div style={{ color: '#94a3b8' }}>by {e.user} • {e.time}</div>
                                    </div>
                                </div>
                            ))}
                        </div>
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

import React, { useState, useEffect } from 'react';
import { generateVisitorReportPdf, getPlacementStats } from '../services/reportService';

const ReportsPage = () => {
    const [activeTab, setActiveTab] = useState('visitors');

    // Visitor Logs
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [pdfMessage, setPdfMessage] = useState('');

    // Placement Stats
    const [placementStats, setPlacementStats] = useState(null);

    useEffect(() => {
        // Set default dates to current month
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
        const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];
        setStartDate(firstDay);
        setEndDate(lastDay);

        if (activeTab === 'placements') {
            loadPlacementStats();
        }
    }, [activeTab]);

    const loadPlacementStats = async () => {
        try {
            const res = await getPlacementStats();
            setPlacementStats(res.data);
        } catch (err) {
            console.error(err);
        }
    };

    const handleGeneratePdf = async (e) => {
        e.preventDefault();
        setPdfMessage('Generating...');
        try {
            const res = await generateVisitorReportPdf(startDate, endDate);
            setPdfMessage(res.data.message + ` (${res.data.count} records processed)`);
        } catch (err) {
            setPdfMessage(err.response?.data?.error || 'Failed to generate PDF.');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>System Reports</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'visitors' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('visitors')}
                    >
                        Visitor Logs PDF
                    </button>
                    <button
                        className={`btn ${activeTab === 'placements' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('placements')}
                    >
                        Placement Analytics
                    </button>
                </div>
            </div>

            {activeTab === 'visitors' && (
                <div className="stat-card" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <h3>Generate Visitor Log Report</h3>
                    <p style={{ color: '#666', marginBottom: '20px' }}>Select a date range to generate a comprehensive PDF report of campus visitors.</p>

                    <form className="form-grid" onSubmit={handleGeneratePdf}>
                        <div className="form-group">
                            <label>Start Date</label>
                            <input
                                type="date"
                                required
                                value={startDate}
                                onChange={e => setStartDate(e.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label>End Date</label>
                            <input
                                type="date"
                                required
                                value={endDate}
                                onChange={e => setEndDate(e.target.value)}
                            />
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                                Generate PDF Report
                            </button>
                        </div>
                    </form>

                    {pdfMessage && (
                        <div style={{ marginTop: '20px', padding: '15px', borderRadius: '4px', backgroundColor: pdfMessage.includes('Failed') ? '#ffebee' : '#e8f5e9', color: pdfMessage.includes('Failed') ? '#c62828' : '#2e7d32', border: `1px solid ${pdfMessage.includes('Failed') ? '#ef9a9a' : '#a5d6a7'}` }}>
                            <strong>Result:</strong> {pdfMessage}
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'placements' && placementStats && (
                <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
                    <div className="stat-card" style={{ flex: 1, minWidth: '250px', borderLeft: '4px solid var(--primary-color)' }}>
                        <h3>Total Placement Drives</h3>
                        <h1 style={{ fontSize: '3rem', margin: '10px 0', color: 'var(--primary-color)' }}>{placementStats.totalDrives}</h1>
                        <p>Historical drives conducted</p>
                    </div>

                    <div className="stat-card" style={{ flex: 1, minWidth: '250px', borderLeft: '4px solid #2e7d32' }}>
                        <h3>Active Drives</h3>
                        <h1 style={{ fontSize: '3rem', margin: '10px 0', color: '#2e7d32' }}>{placementStats.activeDrives}</h1>
                        <p>Drives currently accepting applications</p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReportsPage;

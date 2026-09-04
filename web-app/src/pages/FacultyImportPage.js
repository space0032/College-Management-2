import React, { useState, useRef } from 'react';
import { importFaculty, downloadTemplate } from '../services/facultyService';


const FacultyImportPage = () => {
    const [file, setFile] = useState(null);
    const [preview, setPreview] = useState(null);
    const [importing, setImporting] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);
    const fileInputRef = useRef(null);

    const handleFileChange = (e) => {
        const selected = e.target.files[0];
        if (!selected) return;
        setFile(selected);
        setResult(null);
        setError(null);

        // Read file for preview
        const reader = new FileReader();
        reader.onload = (evt) => {
            const text = evt.target.result;
            const lines = text.split('\n').filter(l => l.trim());
            if (lines.length < 2) {
                setError('File must have a header row and at least one data row');
                return;
            }
            const headers = lines[0].split(',').map(h => h.trim());
            const rows = lines.slice(1).map(line => {
                const fields = [];
                let current = '';
                let inQuotes = false;
                for (let i = 0; i < line.length; i++) {
                    const c = line[i];
                    if (c === '"') { inQuotes = !inQuotes; }
                    else if (c === ',' && !inQuotes) { fields.push(current.trim()); current = ''; }
                    else { current += c; }
                }
                fields.push(current.trim());
                return fields;
            });
            setPreview({ headers, rows: rows.slice(0, 10), totalRows: rows.length });
        };
        reader.readAsText(selected);
    };

    const handleImport = async () => {
        if (!file) return;
        setImporting(true);
        setError(null);
        setResult(null);

        try {
            const formData = new FormData();
            formData.append('file', file);
            const res = await importFaculty(formData);
            setResult(res.data);
            setFile(null);
            setPreview(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
        } catch (err) {
            setError(err.response?.data?.error || 'Import failed. Please try again.');
        } finally {
            setImporting(false);
        }
    };

    const handleDownloadTemplate = async () => {
        try {
            const res = await downloadTemplate();
            const blob = new Blob([res.data], { type: 'text/csv' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'faculty_import_template.csv';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } catch (err) {
            setError('Failed to download template');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Bulk Faculty Import</h1>
                    <p className="page-subtitle">Import multiple faculty members at once using CSV or Excel files</p>
                </div>
                <button className="btn btn-secondary" onClick={handleDownloadTemplate}>
                    Download Template
                </button>
            </div>

            {error && <div className="alert alert-danger" style={{ marginBottom: '15px' }}>{error}</div>}

            {result && (
                <div className="card" style={{ padding: '20px', marginBottom: '20px' }}>
                    <h3 style={{ marginTop: 0, color: '#1a202c' }}>Import Results</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px' }}>
                        <div style={{ padding: '15px', background: '#f0fdf4', borderRadius: '8px', border: '1px solid #bbf7d0' }}>
                            <div style={{ fontSize: '0.85rem', color: '#166534' }}>Successfully Imported</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#15803d' }}>{result.imported}</div>
                        </div>
                        <div style={{ padding: '15px', background: result.failed > 0 ? '#fef2f2' : '#f0fdf4', borderRadius: '8px', border: result.failed > 0 ? '1px solid #fca5a5' : '1px solid #bbf7d0' }}>
                            <div style={{ fontSize: '0.85rem', color: result.failed > 0 ? '#991b1b' : '#166534' }}>Failed</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: result.failed > 0 ? '#dc2626' : '#15803d' }}>{result.failed}</div>
                        </div>
                    </div>
                    {result.errors && result.errors.length > 0 && (
                        <div>
                            <h4 style={{ color: '#dc2626' }}>Errors:</h4>
                            <div style={{ maxHeight: '200px', overflowY: 'auto' }}>
                                {result.errors.map((err, i) => (
                                    <div key={i} style={{ padding: '8px', borderBottom: '1px solid #f1f5f9', fontSize: '0.9rem' }}>
                                        <strong>Row {err.row}:</strong> {err.message}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            )}

            <div className="card" style={{ padding: '30px' }}>
                <div
                    onDragOver={e => e.preventDefault()}
                    onDrop={e => { e.preventDefault(); handleFileChange({ target: { files: e.dataTransfer.files } }); }}
                    style={{
                        border: '2px dashed #cbd5e0', borderRadius: '12px', padding: '40px', textAlign: 'center',
                        background: file ? '#f7fafc' : 'white', cursor: 'pointer', transition: 'all 0.2s'
                    }}
                    onClick={() => fileInputRef.current?.click()}
                >
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept=".csv,.xlsx,.xls"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                    />
                    <div style={{ fontSize: '2rem', marginBottom: '10px' }}>{file ? '📄' : '📁'}</div>
                    {file ? (
                        <div>
                            <strong>{file.name}</strong>
                            <p style={{ color: '#666', margin: '5px 0' }}>{(file.size / 1024).toFixed(1)} KB</p>
                            <p style={{ color: '#666', fontSize: '0.85rem' }}>Click to change file</p>
                        </div>
                    ) : (
                        <div>
                            <p style={{ fontSize: '1.1rem', fontWeight: '500' }}>Drop CSV or Excel file here, or click to browse</p>
                            <p style={{ color: '#666', fontSize: '0.85rem' }}>Supported formats: .csv, .xlsx, .xls</p>
                        </div>
                    )}
                </div>
            </div>

            {preview && (
                <div className="card" style={{ padding: '20px', marginTop: '20px' }}>
                    <h3 style={{ marginTop: 0 }}>Preview ({preview.totalRows} rows)</h3>
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table" style={{ width: '100%' }}>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    {preview.headers.map((h, i) => <th key={i}>{h}</th>)}
                                </tr>
                            </thead>
                            <tbody>
                                {preview.rows.map((row, i) => (
                                    <tr key={i}>
                                        <td>{i + 1}</td>
                                        {row.map((cell, j) => <td key={j}>{cell}</td>)}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    {preview.totalRows > 10 && (
                        <p style={{ color: '#666', fontSize: '0.85rem', marginTop: '10px' }}>
                            Showing 10 of {preview.totalRows} rows
                        </p>
                    )}
                    <button
                        className="btn btn-primary"
                        onClick={handleImport}
                        disabled={importing}
                        style={{ marginTop: '15px' }}
                    >
                        {importing ? 'Importing...' : `Import ${preview.totalRows} Faculty Members`}
                    </button>
                </div>
            )}

            <div className="card" style={{ padding: '20px', marginTop: '20px' }}>
                <h3 style={{ marginTop: 0 }}>CSV Format Instructions</h3>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                    <div>
                        <h4 style={{ color: '#666' }}>Required Columns</h4>
                        <ul style={{ color: '#4a5568' }}>
                            <li><strong>Name</strong> - Full name of the faculty member</li>
                            <li><strong>Email</strong> - Valid email address (must be unique)</li>
                        </ul>
                    </div>
                    <div>
                        <h4 style={{ color: '#666' }}>Optional Columns</h4>
                        <ul style={{ color: '#4a5568' }}>
                            <li><strong>Phone</strong> - Contact number</li>
                            <li><strong>Department</strong> - Department name</li>
                            <li><strong>Qualification</strong> - e.g., PhD, M.Tech</li>
                            <li><strong>Specialization</strong> - Area of specialization</li>
                            <li><strong>Password</strong> - Login password (default: 123)</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FacultyImportPage;

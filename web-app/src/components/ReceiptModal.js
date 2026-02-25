import React from 'react';

const ReceiptModal = ({ fee, onClose }) => {
    const collegeName = localStorage.getItem('collegeName') || 'College Management System';
    const receiptId = `RCP-${fee.id}-${Date.now().toString(36).toUpperCase()}`;
    const today = new Date().toLocaleDateString('en-IN', {
        year: 'numeric', month: 'long', day: 'numeric'
    });

    const handlePrint = () => {
        window.print();
    };

    return (
        <div className="modal-overlay" id="receipt-modal-overlay">
            <div
                className="modal-content"
                style={{ maxWidth: '500px', fontFamily: 'Georgia, serif' }}
                id="receipt-content"
            >
                {/* Header */}
                <div style={{
                    background: 'linear-gradient(135deg, #1a365d, #3182ce)',
                    color: 'white', padding: '24px', textAlign: 'center',
                    borderRadius: '8px 8px 0 0'
                }}>
                    <div style={{ fontSize: '2rem', marginBottom: '6px' }}>🎓</div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '4px' }}>{collegeName}</div>
                    <div style={{ fontSize: '0.85rem', opacity: 0.85 }}>Official Fee Payment Receipt</div>
                </div>

                {/* Receipt Details */}
                <div style={{ padding: '20px 24px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 20px', marginBottom: '20px', fontSize: '0.88rem' }}>
                        <div><span style={{ color: '#718096' }}>Receipt No:</span><br /><strong>{receiptId}</strong></div>
                        <div><span style={{ color: '#718096' }}>Date:</span><br /><strong>{today}</strong></div>
                        <div><span style={{ color: '#718096' }}>Student Name:</span><br /><strong>{fee.studentName || 'N/A'}</strong></div>
                        <div><span style={{ color: '#718096' }}>Student ID:</span><br /><strong>{fee.studentId || fee.id}</strong></div>
                    </div>

                    <hr style={{ border: 'none', borderTop: '1px dashed #e2e8f0', margin: '16px 0' }} />

                    {/* Itemized table */}
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
                        <thead>
                            <tr style={{ background: '#f7fafc' }}>
                                <th style={{ padding: '8px 10px', textAlign: 'left', border: '1px solid #e2e8f0' }}>Fee Type</th>
                                <th style={{ padding: '8px 10px', textAlign: 'right', border: '1px solid #e2e8f0' }}>Amount</th>
                                <th style={{ padding: '8px 10px', textAlign: 'center', border: '1px solid #e2e8f0' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td style={{ padding: '8px 10px', border: '1px solid #e2e8f0' }}>{fee.feeType || fee.categoryName || 'Tuition Fee'}</td>
                                <td style={{ padding: '8px 10px', textAlign: 'right', border: '1px solid #e2e8f0', fontWeight: 'bold' }}>
                                    ₹{parseFloat(fee.amount || fee.totalAmount || 0).toLocaleString('en-IN')}
                                </td>
                                <td style={{ padding: '8px 10px', textAlign: 'center', border: '1px solid #e2e8f0' }}>
                                    <span style={{
                                        background: '#f0fff4', color: '#276749',
                                        padding: '2px 8px', borderRadius: '10px', fontSize: '0.8rem', fontWeight: 'bold'
                                    }}>✓ PAID</span>
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr style={{ background: '#ebf8ff' }}>
                                <td style={{ padding: '10px', fontWeight: 'bold', border: '1px solid #e2e8f0' }}>Total Paid</td>
                                <td colSpan="2" style={{ padding: '10px', textAlign: 'right', fontWeight: 'bold', fontSize: '1rem', color: '#2b6cb0', border: '1px solid #e2e8f0' }}>
                                    ₹{parseFloat(fee.paidAmount || fee.amount || fee.totalAmount || 0).toLocaleString('en-IN')}
                                </td>
                            </tr>
                        </tfoot>
                    </table>

                    {/* Receipt ID - decorative */}
                    <div style={{
                        marginTop: '20px', padding: '10px', background: '#f7fafc',
                        borderRadius: '6px', textAlign: 'center', border: '1px dashed #cbd5e0'
                    }}>
                        <div style={{ fontSize: '0.75rem', color: '#718096', marginBottom: '4px' }}>
                            Transaction Reference
                        </div>
                        <div style={{ fontFamily: 'monospace', fontSize: '0.95rem', fontWeight: 'bold', letterSpacing: '1px', color: '#2d3748' }}>
                            {receiptId}
                        </div>
                    </div>

                    <p style={{ fontSize: '0.75rem', color: '#a0aec0', textAlign: 'center', marginTop: '12px' }}>
                        This is a computer-generated receipt and is valid without a signature.
                    </p>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', gap: '10px', padding: '16px 24px', borderTop: '1px solid #e2e8f0' }}>
                    <button className="btn btn-secondary" style={{ flex: 1 }} onClick={onClose}>Close</button>
                    <button className="btn btn-primary" style={{ flex: 2 }} onClick={handlePrint}>🖨 Print Receipt</button>
                </div>
            </div>
        </div>
    );
};

export default ReceiptModal;

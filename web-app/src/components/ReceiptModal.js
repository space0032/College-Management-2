import React, { useMemo, useState } from 'react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

const formatReceiptDate = (value) => {
    if (!value) {
        return new Date().toLocaleDateString('en-IN', { year: 'numeric', month: 'long', day: 'numeric' });
    }
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return String(value);
    return d.toLocaleDateString('en-IN', { year: 'numeric', month: 'long', day: 'numeric' });
};

const escapeHtml = (value) => String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');

const ReceiptModal = ({ fee, onClose }) => {
    const [pdfError, setPdfError] = useState('');
    const [printError, setPrintError] = useState('');
    const collegeName = localStorage.getItem('collegeName') || 'College Management System';
    // Stable reference: prefer the real backend receipt number. For aggregate
    // StudentFee rows (no single receipt) show a fee reference instead of a
    // fake timestamped receipt that changes on every render.
    const receiptId = useMemo(() => {
        if (fee.receiptNumber) return fee.receiptNumber;
        if (fee.id != null) return `REF-FEE-${fee.id}`;
        return 'REF-NEW';
    }, [fee.receiptNumber, fee.id]);
    const today = useMemo(
        () => formatReceiptDate(fee.paidDate || fee.paymentDate || fee.payment_date || fee.date),
        [fee.paidDate, fee.paymentDate, fee.payment_date, fee.date]
    );

    const downloadReceiptPDF = () => {
        setPdfError('');
        try {
            const doc = new jsPDF();

            // Header
            doc.setFillColor(26, 54, 93);
            doc.rect(0, 0, 210, 40, 'F');
            doc.setTextColor(255, 255, 255);
            doc.setFontSize(22);
            doc.text(collegeName, 105, 20, { align: 'center' });
            doc.setFontSize(12);
            doc.text('Official Fee Payment Receipt', 105, 30, { align: 'center' });

            // Details
            doc.setTextColor(0, 0, 0);
            doc.setFontSize(10);
            doc.text(`Receipt No: ${receiptId}`, 14, 50);
            doc.text(`Date: ${today}`, 140, 50);
            doc.text(`Student Name: ${fee.studentName || 'N/A'}`, 14, 60);
            doc.text(`Enrollment No.: ${fee.studentUsername || fee.studentEnrollmentId || fee.enrollmentNumber || fee.enrollmentId || fee.studentId || fee.id || 'N/A'}`, 14, 66);

            // Table (jspdf-autotable v5 functional API)
            autoTable(doc, {
                startY: 72,
                head: [['Fee Type', 'Amount', 'Status']],
                body: [[
                    fee.feeType || fee.categoryName || 'Tuition Fee',
                    `\u20B9${Number(fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN')}`,
                    'PAID'
                ]],
                theme: 'striped',
                headStyles: { fillColor: [49, 130, 206] }
            });

            const finalY = (doc.lastAutoTable?.finalY ?? 90) + 10;
            doc.setFontSize(12);
            doc.setTextColor(43, 108, 176);
            doc.text(`Total Paid: \u20B9${Number(fee.paidAmount ?? fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN')}`, 196, finalY, { align: 'right' });

            doc.setFontSize(9);
            doc.setTextColor(160, 174, 192);
            doc.text('This is a computer-generated receipt and is valid without a signature.', 105, finalY + 20, { align: 'center' });

            doc.save(`${receiptId}.pdf`);
        } catch (e) {
            setPdfError('Failed to generate PDF. Please try again.');
        }
    };

    const buildPrintDocument = () => {
        const amount = Number(fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN');
        const total = Number(fee.paidAmount ?? fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN');
        const enrollment = fee.studentUsername || fee.studentEnrollmentId || fee.enrollmentNumber
            || fee.enrollmentId || fee.studentId || fee.id || 'N/A';
        const feeType = fee.feeType || fee.categoryName || 'Tuition Fee';
        return `<!DOCTYPE html><html><head><meta charset="utf-8"><title>Fee Receipt ${escapeHtml(receiptId)}</title>` +
            `<style>body{font-family:Georgia,serif;margin:0;padding:24px;color:#1a202c;}` +
            `.receipt{max-width:560px;margin:0 auto;border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;}` +
            `.header{background:#1a365d;color:#fff;padding:24px;text-align:center;-webkit-print-color-adjust:exact;print-color-adjust:exact;}` +
            `.header h1{font-size:1.2rem;margin:8px 0 4px;}.header p{font-size:.85rem;opacity:.85;margin:0;}` +
            `.body{padding:20px 24px;}.grid{display:grid;grid-template-columns:1fr 1fr;gap:8px 20px;margin-bottom:16px;font-size:.88rem;}` +
            `.label{color:#718096;font-size:.75rem;}table{width:100%;border-collapse:collapse;font-size:.88rem;}` +
            `th,td{padding:8px 10px;border:1px solid #e2e8f0;}thead tr{background:#f7fafc;-webkit-print-color-adjust:exact;print-color-adjust:exact;}` +
            `.right{text-align:right;}.center{text-align:center;}tfoot tr{background:#ebf8ff;-webkit-print-color-adjust:exact;print-color-adjust:exact;}` +
            `.total{font-weight:bold;color:#2b6cb0;font-size:1rem;}.ref{margin-top:16px;padding:10px;background:#f7fafc;border:1px dashed #cbd5e0;border-radius:6px;text-align:center;font-family:monospace;font-weight:bold;letter-spacing:1px;}` +
            `.note{font-size:.75rem;color:#a0aec0;text-align:center;margin-top:12px;}` +
            `@media print{body{padding:0;}.receipt{border:none;max-width:100%;}}</style></head><body>` +
            `<div class="receipt"><div class="header"><div style="font-size:2rem;">&#127979;</div>` +
            `<h1>${escapeHtml(collegeName)}</h1><p>Official Fee Payment Receipt</p></div>` +
            `<div class="body"><div class="grid">` +
            `<div><div class="label">Receipt No.</div><strong>${escapeHtml(receiptId)}</strong></div>` +
            `<div><div class="label">Date</div><strong>${escapeHtml(today)}</strong></div>` +
            `<div><div class="label">Student Name</div><strong>${escapeHtml(fee.studentName || 'N/A')}</strong></div>` +
            `<div><div class="label">Enrollment No.</div><strong>${escapeHtml(enrollment)}</strong></div>` +
            `</div><table><thead><tr><th>Fee Type</th><th class="right">Amount</th><th class="center">Status</th></tr></thead>` +
            `<tbody><tr><td>${escapeHtml(feeType)}</td><td class="right"><strong>&#8377;${escapeHtml(amount)}</strong></td><td class="center">&#10003; PAID</td></tr></tbody>` +
            `<tfoot><tr><td><strong>Total Paid</strong></td><td colspan="2" class="right total">&#8377;${escapeHtml(total)}</td></tr></tfoot></table>` +
            `<div class="ref">${escapeHtml(receiptId)}</div>` +
            `<p class="note">This is a computer-generated receipt and is valid without a signature.</p>` +
            `</div></div>` +
            // eslint-disable-next-line no-useless-escape
            `<script>window.onload=function(){window.focus();window.print();};window.onafterprint=function(){window.close();};<\/script>` +
            `</body></html>`;
    };

    const handlePrint = () => {
        setPrintError('');
        try {
            const printWindow = window.open('', '_blank', 'width=700,height=800');
            if (!printWindow) {
                setPrintError('Popup blocked. Please allow popups for this site, then try Print again.');
                return;
            }
            printWindow.document.write(buildPrintDocument());
            printWindow.document.close();
        } catch (e) {
            setPrintError('Failed to open print preview. Please try again.');
        }
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
                        <div><span style={{ color: '#718096' }}>Enrollment No.:</span><br /><strong style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{fee.studentUsername || fee.studentEnrollmentId || fee.enrollmentNumber || fee.enrollmentId || fee.studentId || fee.id || 'N/A'}</strong></div>
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
                                    ₹{Number(fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN')}
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
                                    ₹{Number(fee.paidAmount ?? fee.amount ?? fee.totalAmount ?? 0).toLocaleString('en-IN')}
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
                {pdfError && <div className="alert alert-error" style={{ margin: '0 24px 12px' }}>{pdfError}</div>}
                {printError && <div className="alert alert-error" style={{ margin: '0 24px 12px' }}>{printError}</div>}
                <div id="receipt-actions" style={{ display: 'flex', gap: '10px', padding: '16px 24px', borderTop: '1px solid #e2e8f0' }}>
                    <button className="btn btn-secondary" style={{ flex: 1 }} onClick={onClose}>Close</button>
                    <button className="btn btn-primary" style={{ flex: 2 }} onClick={downloadReceiptPDF}>📥 Download PDF</button>
                    <button className="btn btn-secondary" style={{ flex: 1.5 }} onClick={handlePrint}>🖨 Print</button>
                </div>
            </div>
        </div>
    );
};

export default ReceiptModal;

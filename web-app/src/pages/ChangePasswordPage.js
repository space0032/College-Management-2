import React, { useState } from 'react';
import api from '../services/api';

const ChangePasswordPage = () => {
    const [form, setForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [status, setStatus] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (form.newPassword !== form.confirmPassword) {
            setStatus({ type: 'error', message: 'New passwords do not match.' });
            return;
        }

        setLoading(true);
        setStatus(null);
        try {
            await api.post('/auth/change-password', form);
            setStatus({ type: 'success', message: 'Password updated successfully! Redirecting...' });
            setTimeout(() => window.location.href = '/dashboard/profile', 2000);
        } catch (err) {
            setStatus({ type: 'error', message: err.response?.data?.message || 'Failed to update password.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
            <div className="stat-card" style={{ maxWidth: '450px', width: '100%', padding: '40px' }}>
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '10px' }}>🔐</div>
                    <h2 className="page-title">Credential Security</h2>
                    <p className="text-muted">Update your institutional access credentials</p>
                </div>

                {status && (
                    <div style={{
                        padding: '15px',
                        borderRadius: '10px',
                        marginBottom: '20px',
                        background: status.type === 'success' ? '#f0fdf4' : '#fef2f2',
                        color: status.type === 'success' ? '#166534' : '#991b1b',
                        border: `1px solid ${status.type === 'success' ? '#bbf7d0' : '#fecaca'}`
                    }}>
                        {status.message}
                    </div>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div className="form-group">
                        <label>Current Password</label>
                        <input
                            type="password"
                            className="form-control"
                            required
                            value={form.oldPassword}
                            onChange={e => setForm({ ...form, oldPassword: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>New Password</label>
                        <input
                            type="password"
                            className="form-control"
                            required
                            value={form.newPassword}
                            onChange={e => setForm({ ...form, newPassword: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Confirm New Password</label>
                        <input
                            type="password"
                            className="form-control"
                            required
                            value={form.confirmPassword}
                            onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                        />
                    </div>
                    <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '10px', padding: '15px' }}>
                        {loading ? 'Processing Security Update...' : 'Commit Password Change'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ChangePasswordPage;

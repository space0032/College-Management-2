import React, { useState } from 'react';
import { updatePassword } from '../services/authService';

const ProfilePage = () => {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
  })();

  const initials = (user.username || user.name || 'U')
    .split(' ')
    .map((w) => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState(null);
  const [isError, setIsError] = useState(false);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setMessage(null);
    setIsError(false);

    if (newPassword !== confirmPassword) {
      setMessage("New passwords do not match.");
      setIsError(true);
      return;
    }

    try {
      await updatePassword(user.id, oldPassword, newPassword);
      setMessage("Password updated successfully!");
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setMessage(err.response?.data?.error || "Failed to update password.");
      setIsError(true);
    }
  };

  const fields = [
    { label: 'Username', value: user.username },
    { label: 'Full Name', value: user.name },
    { label: 'Email', value: user.email },
    { label: 'Role', value: user.role },
    { label: 'User ID', value: user.id },
    { label: 'Department', value: user.department },
  ].filter((f) => f.value);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👤 My Profile</h1>
      </div>
      <div className="card profile-card">
        <div className="card-body">
          <div className="profile-avatar">{initials}</div>
          <dl className="profile-info">
            {fields.map((f) => (
              <React.Fragment key={f.label}>
                <dt>{f.label}</dt>
                <dd>{String(f.value)}</dd>
              </React.Fragment>
            ))}
          </dl>
          {fields.length === 0 && (
            <p style={{ color: 'var(--text-secondary)' }}>No user information available.</p>
          )}

          <div style={{ marginTop: '30px', borderTop: '1px solid #eee', paddingTop: '20px' }}>
            <h3 style={{ marginBottom: '15px' }}>🔒 Change Password</h3>
            {message && (
              <div style={{ padding: '10px', marginBottom: '15px', borderRadius: '4px', backgroundColor: isError ? '#ffebee' : '#e8f5e9', color: isError ? '#c62828' : '#2e7d32' }}>
                {message}
              </div>
            )}
            <form onSubmit={handlePasswordChange} style={{ maxWidth: '400px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <div className="form-group">
                <label>Current Password</label>
                <input required type="password" value={oldPassword} onChange={e => setOldPassword(e.target.value)} />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input required type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
              </div>
              <div className="form-group">
                <label>Confirm New Password</label>
                <input required type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>Update Password</button>
            </form>
          </div>

        </div>
      </div>
    </div>
  );
};

export default ProfilePage;

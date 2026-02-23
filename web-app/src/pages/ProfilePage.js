import React from 'react';

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
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;

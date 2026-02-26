import React, { useState, useEffect } from 'react';
import { updatePassword } from '../services/authService';
import api from '../services/api';

const AVATAR_COLORS = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)',
  'linear-gradient(135deg, #30cfd0, #330867)',
];

const getAvatarColor = (name = '') => {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
};

const ProfilePage = () => {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
  })();

  const isStudent = user.role === 'STUDENT';
  const isFaculty = user.role === 'FACULTY';

  const initials = (user.name || user.username || 'U')
    .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

  // Password change state
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [pwMessage, setPwMessage] = useState(null);
  const [isError, setIsError] = useState(false);

  const getPwStrength = (pw) => {
    if (!pw) return null;
    let score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    if (score <= 1) return { label: 'Weak', color: '#ef4444', width: '33%' };
    if (score <= 2) return { label: 'Medium', color: '#f59e0b', width: '66%' };
    return { label: 'Strong', color: '#10b981', width: '100%' };
  };
  const pwStrength = getPwStrength(newPassword);

  // Role-specific profile data
  const [profileData, setProfileData] = useState(null);
  const [activeTab, setActiveTab] = useState('info');
  const [isEditingInfo, setIsEditingInfo] = useState(false);
  const [editForm, setEditForm] = useState({ phone: '', address: '' });
  const [infoMessage, setInfoMessage] = useState(null);

  useEffect(() => {
    if (isStudent) {
      api.get('/students').then(res => {
        const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
        const found = list.find(s => s.userId === user.id || s.email === user.email);
        if (found) setProfileData(found);
      }).catch(() => { });
    } else if (isFaculty) {
      api.get('/faculty').then(res => {
        const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
        const found = list.find(f => f.userId === user.id || f.email === user.email || f.name === user.name);
        if (found) setProfileData(found);
      }).catch(() => { });
    }
  }, []); // eslint-disable-line

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setPwMessage(null);
    setIsError(false);
    if (newPassword !== confirmPassword) {
      setPwMessage('New passwords do not match.');
      setIsError(true);
      return;
    }
    if (newPassword.length < 8) {
      setPwMessage('Password must be at least 8 characters long.');
      setIsError(true);
      return;
    }
    try {
      await updatePassword(user.id, oldPassword, newPassword);
      setPwMessage('Password updated successfully!');
      setOldPassword(''); setNewPassword(''); setConfirmPassword('');
    } catch (err) {
      setPwMessage(err.response?.data?.error || 'Failed to update password.');
      setIsError(true);
    }
  };

  const tabStyle = (tab) => ({
    padding: '9px 20px', border: 'none',
    borderBottom: activeTab === tab ? '3px solid #3b82f6' : '3px solid transparent',
    background: 'none', cursor: 'pointer',
    fontWeight: activeTab === tab ? '600' : '400',
    color: activeTab === tab ? '#3b82f6' : '#718096',
    fontSize: '0.9rem', transition: 'all 0.15s'
  });

  const InfoRow = ({ label, value }) => value ? (
    <div style={{ display: 'flex', borderBottom: '1px solid #f0f0f0', padding: '10px 0', gap: '16px' }}>
      <span style={{ width: '180px', color: '#718096', fontSize: '0.85rem', flexShrink: 0 }}>{label}</span>
      <span style={{ color: '#2d3748', fontWeight: '500', fontSize: '0.9rem' }}>{String(value)}</span>
    </div>
  ) : null;

  return (
    <div className="page-container" style={{ maxWidth: '760px' }}>
      {/* Avatar + Name Banner */}
      <div style={{
        background: getAvatarColor(user.name || user.username),
        borderRadius: '14px', padding: '28px 32px', marginBottom: '24px',
        display: 'flex', alignItems: 'center', gap: '24px', color: 'white'
      }}>
        <div style={{
          width: '80px', height: '80px', borderRadius: '50%',
          background: 'rgba(255,255,255,0.3)', backdropFilter: 'blur(10px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '1.8rem', fontWeight: 'bold', flexShrink: 0,
          border: '3px solid rgba(255,255,255,0.5)'
        }}>{initials}</div>
        <div>
          <div style={{ fontSize: '1.5rem', fontWeight: '700', marginBottom: '4px' }}>
            {user.name || user.username || 'User'}
          </div>
          <div style={{ opacity: 0.85, fontSize: '0.9rem', marginBottom: '4px' }}>
            <span style={{
              background: 'rgba(255,255,255,0.25)', padding: '2px 10px',
              borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600'
            }}>{user.role}</span>
            {user.department && <span style={{ marginLeft: '12px' }}>🏛 {user.department}</span>}
          </div>
          <div style={{ opacity: 0.8, fontSize: '0.82rem' }}>{user.email}</div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '22px', display: 'flex' }}>
        <button style={tabStyle('info')} onClick={() => setActiveTab('info')}>📋 Profile Info</button>
        {(isStudent || isFaculty) && (
          <button style={tabStyle('role')} onClick={() => setActiveTab('role')}>
            {isStudent ? '🎓 Academic Details' : '👩‍🏫 Faculty Details'}
          </button>
        )}
        <button style={tabStyle('security')} onClick={() => setActiveTab('security')}>🔒 Security</button>
      </div>

      {/* Profile Info Tab */}
      {activeTab === 'info' && (
        <div style={{ background: 'white', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '20px 24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
            <h3 style={{ margin: 0 }}>Basic Information</h3>
            {profileData && (
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  if (isEditingInfo) {
                    // Mock save
                    setProfileData({ ...profileData, phone: editForm.phone, address: editForm.address });
                    setInfoMessage('Profile info updated successfully!');
                    setIsEditingInfo(false);
                    setTimeout(() => setInfoMessage(null), 3000);
                  } else {
                    setIsEditingInfo(true);
                  }
                }}
              >
                {isEditingInfo ? '💾 Save Changes' : '✏️ Edit Contact Info'}
              </button>
            )}
          </div>

          {infoMessage && (
            <div style={{ padding: '10px', background: '#f0fdf4', color: '#166534', borderRadius: '6px', marginBottom: '15px', border: '1px solid #bbf7d0' }}>
              {infoMessage}
            </div>
          )}

          <InfoRow label="Full Name" value={user.name} />
          <InfoRow label="Username" value={user.username} />
          <InfoRow label="Email" value={user.email} />
          <InfoRow label="Role" value={user.role} />
          <InfoRow label="Department" value={user.department} />
          <InfoRow label="User ID" value={user.id} />
          {profileData && (
            isEditingInfo ? (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: '150px 1fr', gap: '10px', padding: '12px 0', borderBottom: '1px solid #edf2f7' }}>
                  <div style={{ color: '#718096', fontWeight: '500' }}>Phone</div>
                  <input type="text" className="form-control" value={editForm.phone} onChange={e => setEditForm({ ...editForm, phone: e.target.value })} style={{ padding: '6px 10px' }} />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '150px 1fr', gap: '10px', padding: '12px 0', borderBottom: '1px solid #edf2f7' }}>
                  <div style={{ color: '#718096', fontWeight: '500' }}>Address</div>
                  <input type="text" className="form-control" value={editForm.address} onChange={e => setEditForm({ ...editForm, address: e.target.value })} style={{ padding: '6px 10px' }} />
                </div>
              </>
            ) : (
              <>
                <InfoRow label="Phone" value={profileData.phone} />
                <InfoRow label="Address" value={profileData.address} />
              </>
            )
          )}
          {(!user.name && !user.email) && (
            <p style={{ color: '#a0aec0', textAlign: 'center', padding: '20px' }}>No profile information found.</p>
          )}
        </div>
      )}

      {/* Student-specific tab */}
      {activeTab === 'role' && isStudent && (
        <div style={{ background: 'white', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '20px 24px' }}>
          {profileData ? (<>
            <InfoRow label="Enrollment Number" value={profileData.enrollmentNumber || profileData.enrollment_number} />
            <InfoRow label="Course" value={profileData.course} />
            <InfoRow label="Semester" value={profileData.semester} />
            <InfoRow label="Batch Year" value={profileData.batchYear || profileData.batch_year} />
            <InfoRow label="Gender" value={profileData.gender} />
            <InfoRow label="Date of Birth" value={profileData.dateOfBirth || profileData.date_of_birth} />
            <InfoRow label="Hostelite" value={profileData.hostelite != null ? (profileData.hostelite ? 'Yes' : 'No') : undefined} />
            <InfoRow label="Guardian Name" value={profileData.guardianName || profileData.guardian_name} />
            <InfoRow label="Guardian Phone" value={profileData.guardianPhone || profileData.guardian_phone} />
            <InfoRow label="Blood Group" value={profileData.bloodGroup || profileData.blood_group} />
          </>) : (
            <p style={{ color: '#a0aec0', textAlign: 'center', padding: '20px' }}>No student profile data found.</p>
          )}
        </div>
      )}

      {/* Faculty-specific tab */}
      {activeTab === 'role' && isFaculty && (
        <div style={{ background: 'white', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '20px 24px' }}>
          {profileData ? (<>
            <InfoRow label="Employee ID" value={profileData.employeeId || profileData.employee_id} />
            <InfoRow label="Qualification" value={profileData.qualification} />
            <InfoRow label="Specialization" value={profileData.specialization} />
            <InfoRow label="Experience (years)" value={profileData.experience} />
            <InfoRow label="Designation" value={profileData.designation} />
            <InfoRow label="Department" value={profileData.department} />
            <InfoRow label="Joining Date" value={profileData.joiningDate || profileData.joining_date} />
            <InfoRow label="Subjects Taught" value={profileData.subjects || profileData.subjectsTaught} />
          </>) : (
            <p style={{ color: '#a0aec0', textAlign: 'center', padding: '20px' }}>No faculty profile data found.</p>
          )}
        </div>
      )}

      {/* Security / Password Tab */}
      {activeTab === 'security' && (
        <div style={{ background: 'white', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '24px' }}>
          <h3 style={{ marginBottom: '20px', color: '#2d3748' }}>🔒 Change Password</h3>
          {pwMessage && (
            <div style={{
              padding: '10px 14px', marginBottom: '16px', borderRadius: '6px',
              background: isError ? '#fff5f5' : '#f0fff4',
              color: isError ? '#c53030' : '#276749',
              border: `1px solid ${isError ? '#feb2b2' : '#9ae6b4'}`
            }}>{pwMessage}</div>
          )}
          <form onSubmit={handlePasswordChange} style={{ maxWidth: '380px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div className="form-group">
              <label>Current Password</label>
              <input required type="password" value={oldPassword} onChange={e => setOldPassword(e.target.value)} />
            </div>
            <div className="form-group">
              <label>New Password</label>
              <input required type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
              {pwStrength && (
                <div style={{ marginTop: '6px' }}>
                  <div style={{ height: '5px', borderRadius: '4px', background: '#e2e8f0', overflow: 'hidden' }}>
                    <div style={{ width: pwStrength.width, height: '100%', background: pwStrength.color, transition: 'width 0.3s, background 0.3s', borderRadius: '4px' }} />
                  </div>
                  <div style={{ fontSize: '0.75rem', marginTop: '4px', color: pwStrength.color, fontWeight: '600' }}>
                    {pwStrength.label} — {newPassword.length < 8 ? 'Min 8 chars' : 'Use uppercase, numbers & symbols for a stronger password.'}
                  </div>
                </div>
              )}
            </div>
            <div className="form-group">
              <label>Confirm New Password</label>
              <input required type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
            </div>
            <button type="submit" className="btn btn-primary" style={{ alignSelf: 'flex-start', marginTop: '4px' }}>
              Update Password
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;

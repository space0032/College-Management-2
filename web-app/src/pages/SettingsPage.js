import React, { useState, useEffect } from 'react';
import { getSettings, updateSettings } from '../services/settingsService';

const SettingsPage = () => {
    const [settings, setSettings] = useState({
        college_name: '',
        college_name: '',
        college_logo_url: '',
        google_drive_folder_id: '',
        timezone: '',
        default_theme: 'light'
    });

    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState(null);

    const userRole = localStorage.getItem('userRole');

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        try {
            const res = await getSettings();
            if (res.data) {
                setSettings({
                    college_name: res.data.college_name || '',
                    college_name: res.data.college_name || '',
                    college_logo_url: res.data.college_logo_url || '',
                    google_drive_folder_id: res.data.google_drive_folder_id || '',
                    timezone: res.data.timezone || 'UTC',
                    default_theme: res.data.default_theme || 'light'
                });
            }
        } catch (err) {
            console.error('Failed to load settings', err);
        }
    };

    const handleChange = (e) => {
        setSettings({ ...settings, [e.target.name]: e.target.value });
    };

    const handleSave = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        setMessage(null);
        try {
            await updateSettings(settings);
            setMessage({ type: 'success', text: 'Settings updated successfully!' });

            // Update CSS variables or layout if theme or logo changes
            if (settings.college_name) {
                document.title = settings.college_name + ' SMS';
            }
        } catch (err) {
            setMessage({ type: 'error', text: 'Failed to update settings.' });
        } finally {
            setIsSaving(false);
        }
    };

    if (userRole !== 'ADMIN') {
        return (
            <div className="page-container">
                <div className="stat-card" style={{ textAlign: 'center', padding: '40px', color: '#dc3545' }}>
                    <h2>Access Denied</h2>
                    <p>You do not have administrative privileges to view or modify system settings.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>⚙️ College Settings</h2>
                <p className="text-muted">Manage system-wide configuration, branding, and external integrations.</p>
            </div>

            <div className="form-container" style={{ maxWidth: '800px', margin: '0 auto' }}>
                <div className="stat-card">

                    {message && (
                        <div style={{
                            padding: '12px',
                            marginBottom: '20px',
                            borderRadius: '6px',
                            backgroundColor: message.type === 'success' ? '#e8f5e9' : '#ffebee',
                            color: message.type === 'success' ? '#2e7d32' : '#c62828'
                        }}>
                            {message.text}
                        </div>
                    )}

                    <form onSubmit={handleSave} className="form-grid">

                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '15px' }}>General Branding</h3>
                        </div>

                        <div className="form-group">
                            <label>College Name</label>
                            <input
                                type="text"
                                name="college_name"
                                value={settings.college_name}
                                onChange={handleChange}
                                placeholder="e.g. Oxford University"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>College Logo URL</label>
                            <input
                                type="url"
                                name="college_logo_url"
                                value={settings.college_logo_url}
                                onChange={handleChange}
                                placeholder="https://example.com/logo.png"
                            />
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '20px' }}>
                            <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '15px' }}>System Preferences</h3>
                        </div>

                        <div className="form-group">
                            <label>Default Timezone</label>
                            <select name="timezone" value={settings.timezone} onChange={handleChange}>
                                <option value="UTC">UTC (Universal)</option>
                                <option value="Asia/Kolkata">IST (Asia/Kolkata)</option>
                                <option value="America/New_York">EST (America/New_York)</option>
                                <option value="Europe/London">GMT (Europe/London)</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Default UI Theme</label>
                            <select name="default_theme" value={settings.default_theme} onChange={handleChange}>
                                <option value="light">Light Mode</option>
                                <option value="dark">Dark Mode</option>
                                <option value="auto">System Default (Auto)</option>
                            </select>
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '20px' }}>
                            <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '15px' }}>Integrations</h3>
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Google Drive Folder ID (Cloud Storage)</label>
                            <input
                                type="password"
                                name="google_drive_folder_id"
                                value={settings.google_drive_folder_id}
                                onChange={handleChange}
                                placeholder="Enter Folder ID to enable Google Drive storage..."
                            />
                            <small style={{ color: 'var(--text-muted)' }}>Required for cloud backups and large assignment submissions.</small>
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '20px' }}>
                            <button disabled={isSaving} type="submit" className="btn btn-primary" style={{ padding: '12px 24px', fontSize: '1.1rem' }}>
                                {isSaving ? 'Saving Changes...' : 'Save Configuration'}
                            </button>
                        </div>

                    </form>
                </div>
            </div>
        </div>
    );
};

export default SettingsPage;

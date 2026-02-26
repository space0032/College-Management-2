import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect } from 'react';
import { getSettings, updateSettings } from '../services/settingsService';

const SettingsPage = () => {
    const [settings, setSettings] = useState({
        college_name: '',
        college_logo_url: '',
        dropbox_api_key: '',
        timezone: '',
        default_theme: 'light'
    });

    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState(null);

    const userRole = SessionManager.getUserRole() || 'STUDENT';

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        try {
            const res = await getSettings();
            if (res.data) {
                setSettings({
                    college_name: res.data.college_name || '',
                    college_logo_url: res.data.college_logo_url || '',
                    dropbox_api_key: res.data.dropbox_api_key || '',
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
                            <label>Branding Accent Color</label>
                            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                                <input
                                    type="color"
                                    name="accent_color"
                                    value={settings.accent_color || '#6366f1'}
                                    onChange={handleChange}
                                    style={{ width: '50px', height: '40px', padding: '2px', border: '1px solid #e2e8f0' }}
                                />
                                <input
                                    type="text"
                                    className="form-control"
                                    name="accent_color"
                                    value={settings.accent_color || '#6366f1'}
                                    onChange={handleChange}
                                    style={{ flex: 1 }}
                                />
                            </div>
                        </div>

                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Institutional Logo Enhancement</label>
                            <div style={{ display: 'flex', gap: '20px', alignItems: 'center', background: '#f8fafc', padding: '20px', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
                                <div style={{ width: '80px', height: '80px', background: 'white', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
                                    {settings.college_logo_url ? <img src={settings.college_logo_url} alt="Preview" style={{ maxWidth: '100%', maxHeight: '100%' }} /> : <span style={{ fontSize: '2rem' }}>🏫</span>}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <input
                                        type="url"
                                        name="college_logo_url"
                                        className="form-control"
                                        value={settings.college_logo_url}
                                        onChange={handleChange}
                                        placeholder="Enter direct URL to your institutional logo..."
                                    />
                                    <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '8px' }}>Recommended size: 512x512px. Supports PNG, SVG, and JPEG.</p>
                                </div>
                            </div>
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
                            <label>Dropbox API Key (Cloud Storage)</label>
                            <input
                                type="password"
                                name="dropbox_api_key"
                                value={settings.dropbox_api_key}
                                onChange={handleChange}
                                placeholder="Enter API Key to enable cloud backups..."
                            />
                            <small style={{ color: 'var(--text-muted)' }}>Required for automated database backups and large assignment submissions.</small>
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

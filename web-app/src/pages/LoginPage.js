import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../services/authService';
import SessionManager from '../utils/SessionManager';

const LoginPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.password) {
      setError('Please enter both username and password.');
      return;
    }
    setLoading(true);
    try {
      const response = await loginUser(form);
      const { token, user } = response.data;
      SessionManager.setSession(user, token);
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Login failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-shell">
        <section className="login-story">
          <div className="login-brand"><span className="brand-mark">C</span><strong>CampusOne</strong></div>
          <div className="login-story-content">
            <span className="login-kicker">One connected campus</span>
            <h1>Everything your institution needs, in one place.</h1>
            <p>Bring academics, administration, student services, and campus life together in a workspace built for clarity.</p>
            <div className="login-proof">
              <div><strong>40+</strong><span>Campus modules</span></div>
              <div><strong>3</strong><span>Purpose-built portals</span></div>
              <div><strong>24/7</strong><span>Secure access</span></div>
            </div>
          </div>
          <p className="login-story-footer">Designed for modern higher education</p>
        </section>
        <section className="login-panel">
          <div className="login-card">
            <div className="login-logo">
              <span className="login-kicker">Welcome back</span>
              <h2>Sign in to your portal</h2>
              <p>Use your institutional credentials to continue.</p>
            </div>
            <form className="login-form" onSubmit={handleSubmit}>
              {error && <div className="alert alert-error" role="alert">{error}</div>}
              <div className="form-group">
                <label className="form-label" htmlFor="username">Username</label>
                <input id="username" name="username" type="text" className="form-control"
                  placeholder="e.g. admin or student ID" value={form.username} onChange={handleChange}
                  autoComplete="username" autoFocus />
              </div>
              <div className="form-group">
                <div className="label-row"><label className="form-label" htmlFor="password">Password</label><span>Case sensitive</span></div>
                <input id="password" name="password" type="password" className="form-control"
                  placeholder="Enter your password" value={form.password} onChange={handleChange}
                  autoComplete="current-password" />
              </div>
              <button type="submit" className="btn btn-primary login-submit" disabled={loading}>
                {loading ? <><span className="button-spinner" /> Signing in…</> : <>Continue to dashboard <span>→</span></>}
              </button>
            </form>
            <div className="login-help"><span className="status-dot" /> Protected institutional access</div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default LoginPage;

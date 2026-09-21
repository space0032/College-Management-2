import React, { useCallback, useEffect, useRef, useState } from 'react';
import '../pages/Management.css';

export const errorMessage = error => error?.response?.data?.error || error?.response?.data?.message || error?.message || 'Could not complete the request. Please retry.';
export const currency = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 2 }).format(Number(value) || 0);

export function useManagementData(loader, enabled = true) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState('');
  const [loaded, setLoaded] = useState(false);
  const [source, setSource] = useState(null);
  const request = useRef(0);
  const reload = useCallback(async () => {
    const id = ++request.current;
    setData([]); setLoaded(false); setError('');
    if (!enabled) { setLoading(false); return; }
    setLoading(true);
    try {
      const result = await loader();
      if (id === request.current) { setData(result); setLoaded(true); setSource(() => loader); }
    } catch (err) { if (id === request.current) setError(errorMessage(err)); }
    finally { if (id === request.current) setLoading(false); }
  }, [loader, enabled]);
  const invalidate = useCallback(() => { request.current++; }, []);
  useEffect(() => { reload(); return invalidate; }, [reload, invalidate]);
  return { data: source === loader ? data : [], loading, error, loaded: loaded && source === loader, reload };
}

export function useManagementAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const lock = useRef(false);
  const run = async (action, success = 'Changes saved.') => {
    if (lock.current) return false;
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try { await action(); setNotice(success); return true; }
    catch (err) { setError(errorMessage(err)); return false; }
    finally { lock.current = false; setBusy(false); }
  };
  return { busy, error, notice, run, clear: () => { setError(''); setNotice(''); } };
}

export function Feedback({ error, notice, onRetry }) {
  return <>{error && <div className="management-feedback alert alert-error" role="alert"><span>{error}</span>{onRetry && <button className="btn btn-secondary btn-sm" onClick={onRetry}>Retry</button>}</div>}{notice && <div className="management-feedback alert alert-success" role="status">{notice}</div>}</>;
}
export function Empty({ title = 'No records found', children }) { return <div className="management-empty"><h3>{title}</h3><p>{children}</p></div>; }
export function Loading() { return <div className="loading-container" role="status"><div className="spinner" /><span>Loading records...</span></div>; }
export function Stats({ items }) { return <div className="management-stats">{items.map(([label, value]) => <div key={label}><span>{label}</span><strong>{value}</strong></div>)}</div>; }

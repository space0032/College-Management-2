import React, { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';

const ToastContext = createContext(null);

let externalPush = null;
let seq = 0;

export const toast = {
  success(message, opts = {}) {
    externalPush?.({ kind: 'success', message, refId: opts.refId, title: opts.title || 'Success' });
  },
  error(message, opts = {}) {
    externalPush?.({ kind: 'error', message, refId: opts.refId, title: opts.title || 'Something went wrong', details: opts.details });
  },
  info(message, opts = {}) {
    externalPush?.({ kind: 'info', message, refId: opts.refId, title: opts.title || 'Note' });
  },
};

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) return toast;
  return ctx;
}

export function ToastProvider({ children }) {
  const [items, setItems] = useState([]);
  const timers = useRef(new Map());

  const dismiss = useCallback((id) => {
    setItems((prev) => prev.filter((t) => t.id !== id));
    const tm = timers.current.get(id);
    if (tm) {
      clearTimeout(tm);
      timers.current.delete(id);
    }
  }, []);

  const push = useCallback((entry) => {
    const id = `toast-${Date.now()}-${seq++}`;
    const item = { id, ...entry };
    setItems((prev) => [...prev.slice(-3), item]);
    const tm = setTimeout(() => dismiss(id), entry.kind === 'error' ? 7000 : 4500);
    timers.current.set(id, tm);
  }, [dismiss]);

  externalPush = push;

  const value = useMemo(() => ({ ...toast, dismiss, items }), [dismiss, items]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-region" role="status" aria-live="polite" aria-atomic="false">
        {items.map((t) => (
          <div key={t.id} className={`toast toast-${t.kind}`} role={t.kind === 'error' ? 'alert' : 'status'}>
            <div className="toast-icon" aria-hidden="true">
              {t.kind === 'success' ? '✓' : t.kind === 'error' ? '!' : 'i'}
            </div>
            <div className="toast-body">
              <div className="toast-title">{t.title}</div>
              <div className="toast-message">{t.message}</div>
              {t.refId && <div className="toast-ref">Reference: {t.refId}</div>}
              {t.details && (
                <details className="toast-details">
                  <summary>Technical details{t.details.status ? ` (HTTP ${t.details.status})` : ''}</summary>
                  <pre>{typeof t.details === 'string' ? t.details : JSON.stringify(t.details, null, 2)}</pre>
                </details>
              )}
            </div>
            <button className="toast-close" aria-label="Dismiss notification" onClick={() => dismiss(t.id)}>×</button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export default ToastProvider;

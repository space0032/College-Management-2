import React, { useEffect, useRef } from 'react';

const Modal = ({ isOpen, title, onClose, onSubmit, children, submitLabel = 'Save', submitting = false, size = '', submitDisabled = false }) => {
  const modalRef = useRef(null);
  const firstFieldRef = useRef(null);

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape' && !submitting) onClose();
      // Simple focus trap: keep Tab inside the dialog
      if (e.key === 'Tab' && modalRef.current) {
        const focusable = modalRef.current.querySelectorAll(
          'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
        );
        if (focusable.length === 0) return;
        const first = focusable[0];
        const last = focusable[focusable.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    if (isOpen) {
      document.addEventListener('keydown', handleKey);
      document.body.style.overflow = 'hidden';
      // Autofocus first required field after mount
      const t = setTimeout(() => {
        const target = modalRef.current?.querySelector('input[required], select[required]') || firstFieldRef.current;
        target?.focus?.({ preventScroll: true });
      }, 30);
      return () => {
        clearTimeout(t);
        document.removeEventListener('keydown', handleKey);
        document.body.style.overflow = '';
      };
    }
    return () => {
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose, submitting]);

  if (!isOpen) return null;

  const handleFooterSubmit = () => {
    if (submitting || submitDisabled) return;
    // Most legacy modal forms keep their fields in the body while the
    // action button lives in the footer. Trigger native validation here.
    const form = modalRef.current?.querySelector('form');
    if (form && !form.checkValidity()) {
      form.reportValidity();
      return;
    }
    onSubmit?.();
  };

  return (
    <div
      className="modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget && !submitting) onClose(); }}
    >
      <div
        className={`modal${size ? ` modal-${size}` : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        ref={modalRef}
      >
        <div className="modal-header">
          <h2 className="modal-title" id="modal-title" ref={firstFieldRef} tabIndex={-1}>{title}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close" disabled={submitting}>✕</button>
        </div>
        <div className="modal-body">{children}</div>
        {onSubmit && (
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onClose} disabled={submitting}>Cancel</button>
            <button
              className="btn btn-primary"
              disabled={submitting || submitDisabled}
              onClick={handleFooterSubmit}
            >{submitting ? 'Saving…' : submitLabel}</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Modal;

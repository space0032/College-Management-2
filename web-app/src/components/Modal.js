import React, { useEffect } from 'react';

const Modal = ({ isOpen, title, onClose, onSubmit, children, submitLabel = 'Save', submitting = false }) => {
  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.addEventListener('keydown', handleKey);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      className="modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div className="modal-header">
          <h2 className="modal-title" id="modal-title">{title}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">✕</button>
        </div>
        <div className="modal-body">{children}</div>
        {onSubmit && (
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" disabled={submitting} onClick={() => {
              // Most legacy modal forms keep their <form> in the body while the
              // action button lives in the footer. Trigger native validation here.
              const form = document.querySelector('.modal-body form');
              if (form && !form.checkValidity()) {
                form.reportValidity();
                return;
              }
              onSubmit();
            }}>{submitting ? 'Saving…' : submitLabel}</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Modal;

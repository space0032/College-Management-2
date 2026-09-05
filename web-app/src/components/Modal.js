import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

const SIZE_CLASS = {
  sm: 'modal-sm',
  medium: '',
  md: '',
  large: 'modal-large',
  lg: 'modal-large',
  xl: 'modal-xl',
  drawer: 'modal-drawer',
  fullscreen: 'modal-fullscreen',
};

const Modal = ({
  isOpen,
  title,
  onClose,
  onSubmit,
  children,
  submitLabel = 'Save',
  submitting = false,
  size = 'medium',
  submitDisabled = false,
  isDirty = false,
  confirmCloseMessage = 'You have unsaved changes. Discard them?',
  hideFooter = false,
  destructive = false,
}) => {
  const modalRef = useRef(null);
  const onCloseRef = useRef(onClose);
  const submittingRef = useRef(submitting);
  const wasOpenRef = useRef(false);
  const prevFocusRef = useRef(null);

  onCloseRef.current = onClose;
  submittingRef.current = submitting;

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape' && !submittingRef.current) {
        if (modalRef.current?.dataset?.dirty === 'true') {
          // eslint-disable-next-line no-alert
          if (!window.confirm(modalRef.current.dataset.confirmMsg || 'Discard unsaved changes?')) return;
        }
        onCloseRef.current?.();
      }
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
    if (!isOpen) {
      wasOpenRef.current = false;
      return undefined;
    }
    prevFocusRef.current = document.activeElement;
    document.addEventListener('keydown', handleKey);
    document.body.style.overflow = 'hidden';
    let t;
    if (!wasOpenRef.current) {
      t = setTimeout(() => {
        const target = modalRef.current?.querySelector('input[required], select[required], textarea[required]');
        (target || modalRef.current?.querySelector('.modal-title'))?.focus?.({ preventScroll: true });
      }, 30);
    }
    wasOpenRef.current = true;
    return () => {
      clearTimeout(t);
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = '';
      prevFocusRef.current?.focus?.({ preventScroll: true });
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const requestClose = () => {
    if (submitting) return;
    if (isDirty) {
      // eslint-disable-next-line no-alert
      if (!window.confirm(confirmCloseMessage)) return;
    }
    onClose?.();
  };

  const handleFooterSubmit = () => {
    if (submitting || submitDisabled) return;
    const form = modalRef.current?.querySelector('form');
    if (form && !form.checkValidity()) {
      form.reportValidity();
      return;
    }
    onSubmit?.();
  };

  const sizeClass = SIZE_CLASS[size] ?? (size ? `modal-${size}` : '');

  return createPortal(
    <div
      className={`modal-backdrop${size === 'drawer' ? ' modal-backdrop-drawer' : ''}`}
      onClick={(e) => { if (e.target === e.currentTarget) requestClose(); }}
    >
      <div
        className={`modal${sizeClass ? ` ${sizeClass}` : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        ref={modalRef}
        data-dirty={isDirty ? 'true' : 'false'}
        data-confirm-msg={confirmCloseMessage}
      >
        <div className="modal-header">
          <h2 className="modal-title" id="modal-title" tabIndex={-1}>{title}</h2>
          <button className="modal-close" onClick={requestClose} aria-label="Close" disabled={submitting}>✕</button>
        </div>
        <div className="modal-body">{children}</div>
        {onSubmit && !hideFooter && (
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={requestClose} disabled={submitting}>Cancel</button>
            <button
              className={`btn ${destructive ? 'btn-danger' : 'btn-primary'}`}
              disabled={submitting || submitDisabled}
              onClick={handleFooterSubmit}
            >{submitting ? 'Saving…' : submitLabel}</button>
          </div>
        )}
      </div>
    </div>,
    document.body
  );
};

export default Modal;

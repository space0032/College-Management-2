import React from 'react';
import Modal from './Modal';

const ConfirmDialog = ({
  isOpen,
  title = 'Are you sure?',
  message = 'This action cannot be undone.',
  confirmLabel = 'Delete',
  cancelLabel = 'Cancel',
  destructive = true,
  loading = false,
  onConfirm,
  onCancel,
}) => (
  <Modal
    isOpen={isOpen}
    title={title}
    onClose={onCancel}
    onSubmit={onConfirm}
    submitLabel={loading ? 'Working…' : confirmLabel}
    submitting={loading}
    size="sm"
  >
    <p className="confirm-message">{message}</p>
    <div className="confirm-hint">
      {destructive ? 'Only use this for destructive actions. This cannot be undone.' : 'Please confirm to continue.'}
    </div>
  </Modal>
);

export default ConfirmDialog;
// Cancel button is rendered by Modal footer; destructive styling via .modal-sm .btn-primary.danger override in CSS.

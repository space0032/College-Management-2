import React from 'react';

const formatValue = (value) => {
  if (value == null) return '—';
  if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'string') return String(value);
  if (Array.isArray(value)) return value.length ? value.map(formatValue).join(', ') : '—';
  if (typeof value === 'object') {
    const primitives = Object.entries(value)
      .filter(([k, v]) => v != null && typeof v !== 'object')
      .map(([k, v]) => `${k}: ${formatValue(v)}`);
    return primitives.length ? primitives.join(', ') : JSON.stringify(value);
  }
  return String(value);
};

const DataTable = ({ data = [], columns = [], onEdit, onDelete, emptyMessage = 'No records found.', loading = false, error = '', onRetry }) => {
  const hasActions = onEdit || onDelete;

  if (loading) {
    return <div className="table-wrapper"><div className="table-empty"><div className="spinner" /><p>Loading records…</p></div></div>;
  }

  if (error) {
    return <div className="table-wrapper"><div className="table-empty table-error"><p>{error}</p>{onRetry && <button type="button" className="btn btn-secondary btn-sm" onClick={onRetry}>Retry</button>}</div></div>;
  }

  if (data.length === 0) {
    return (
      <div className="table-wrapper">
        <div className="table-empty">
          <div className="table-empty-icon">📭</div>
          <p>{emptyMessage}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              {columns.map((col) => (
                <th key={col.key}>{col.label}</th>
              ))}
              {hasActions && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {data.map((row, rowIndex) => (
              <tr key={row.id ?? rowIndex}>
                {columns.map((col) => (
                  <td key={col.key}>
                    {col.render ? col.render(row[col.key], row) : formatValue(row[col.key])}
                  </td>
                ))}
                {hasActions && (
                  <td>
                    <div className="table-actions">
                      {onEdit && (
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => onEdit(row)}
                          title="Edit"
                        >
                          ✏️ Edit
                        </button>
                      )}
                      {onDelete && (
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => onDelete(row)}
                          title="Delete"
                        >
                          🗑️ Delete
                        </button>
                      )}
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DataTable;

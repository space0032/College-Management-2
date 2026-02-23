import React from 'react';

const DataTable = ({ data = [], columns = [], onEdit, onDelete, emptyMessage = 'No records found.' }) => {
  const hasActions = onEdit || onDelete;

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
                  {col.render ? col.render(row[col.key], row) : String(row[col.key] ?? '—')}
                </td>
              ))}
              {hasActions && (
                <td>
                  <div className="table-actions">
                    {onEdit && (
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => onEdit(row)}
                        title="Edit"
                      >
                        ✏️ Edit
                      </button>
                    )}
                    {onDelete && (
                      <button
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
  );
};

export default DataTable;

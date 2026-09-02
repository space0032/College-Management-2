/**
 * Export table data to a CSV file and trigger browser download.
 * @param {string[]} columns - Column headers
 * @param {any[][]} rows - Array of row arrays (same order as columns)
 * @param {string} filename - Filename without extension (will add .csv)
 */
export const exportToCSV = (columns, rows, filename = 'export') => {
    const escape = (val) => {
        if (val === null || val === undefined) return '';
        const str = String(val);
        // Wrap in quotes if it contains comma, quote, or newline
        if (/[",\n\r]/.test(str)) {
            return '"' + str.replace(/"/g, '""') + '"';
        }
        return str;
    };

    const csvContent = [
        columns.map(escape).join(','),
        ...rows.map(row => row.map(escape).join(','))
    ].join('\r\n');

    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}.csv`);
    document.body.appendChild(link);
    link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
    document.body.removeChild(link);
    // Edge can cancel a blob download when its object URL is revoked in the same task.
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
};

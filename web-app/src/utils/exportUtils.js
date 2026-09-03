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

/**
 * Export table data to an Excel (.xlsx) file and trigger browser download.
 * @param {string[]} columns - Column headers
 * @param {any[][]} rows - Array of row arrays (same order as columns)
 * @param {string} filename - Filename without extension (will add .xlsx)
 */
export const exportToExcel = async (columns, rows, filename = 'export', sheetName = 'Sheet1') => {
    const XLSX = await import('xlsx');
    const data = [
        columns,
        ...rows.map(row => row.map(v => (v === null || v === undefined ? '' : v)))
    ];
    const ws = XLSX.utils.aoa_to_sheet(data);
    // Auto-size columns based on content length.
    ws['!cols'] = columns.map((_, ci) => {
        let max = (columns[ci] || '').toString().length;
        data.slice(1).forEach(row => {
            const len = String(row[ci] ?? '').length;
            if (len > max) max = len;
        });
        return { wch: Math.min(Math.max(max + 2, 10), 40) };
    });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    XLSX.writeFile(wb, `${filename}.xlsx`);
};

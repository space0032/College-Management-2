const fs = require('fs');
const path = require('path');
const data = require('./web-app/lint.json');
let numFiles = 0;
let out = '';
data.forEach(f => {
    if (f.errorCount > 0 || f.warningCount > 0) {
        numFiles++;
        out += `\n=== ${path.basename(f.filePath)} ===\n`;
        f.messages.forEach(m => {
            out += `  Line ${m.line}: ${m.ruleId || 'syntax'} - ${m.message}\n`;
        });
    }
});
out += `\nTotal files with issues: ${numFiles}\n`;
fs.writeFileSync('lint_summary.txt', out);

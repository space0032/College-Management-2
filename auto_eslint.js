const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

walkDir('web-app/src', function (filePath) {
    if (filePath.endsWith('.js')) {
        let content = fs.readFileSync(filePath, 'utf8');
        let original = content;
        // Remove completely unused state lines
        content = content.replace(/^[ \t]*const\s+\[[a-zA-Z0-9_]+,\s*[a-zA-Z0-9_]+\]\s*=\s*useState\([^)]*\);\s*\/\/\s*eslint-disable-line\s+no-unused-vars.*$/gm, '');

        // Remove inline eslint disables
        content = content.replace(/[ \t]*\/\/\s*eslint-disable-line.*/g, '');

        // Remove previous line eslint disables
        content = content.replace(/^[ \t]*\/\/\s*eslint-disable-next-line.*[\r\n]+/gm, '');

        if (original !== content) {
            fs.writeFileSync(filePath, content, 'utf8');
            console.log('Processed:', filePath);
        }
    }
});

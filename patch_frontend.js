const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'web-app', 'src');

const walkSync = (dir, filelist = []) => {
    fs.readdirSync(dir).forEach(file => {
        const filePath = path.join(dir, file);
        if (fs.statSync(filePath).isDirectory()) {
            filelist = walkSync(filePath, filelist);
        } else if (filePath.endsWith('.js') || filePath.endsWith('.jsx')) {
            filelist.push(filePath);
        }
    });
    return filelist;
};

const mapPermission = (fileContent, filePath) => {
    let basename = path.basename(filePath, '.js');
    let mod = fileContent;
    let modified = false;

    // We assume SessionManager is imported, if not we might have a problem but let's just do it
    // Wait, if it says `user.role === 'ADMIN'` we can't easily use SessionManager if it's not imported.
    // However, if we replace `user.role === 'ADMIN'` with `SessionManager.hasRole('ADMIN')` we must ensure import.

    // Let's just patch a few prominent ones that use `SessionManager.getUserRole() === 'ADMIN'`
    if (mod.includes("SessionManager.getUserRole() === 'ADMIN'")) {
        mod = mod.replace(/SessionManager\.getUserRole\(\) === 'ADMIN'/g, "SessionManager.hasRole('ADMIN') /* or hasPermission */");
        modified = true;
    }

    if (mod.includes("user.role === 'ADMIN'")) {
        mod = mod.replace(/user\.role === 'ADMIN'/g, "SessionManager.hasRole('ADMIN')");
        modified = true;
        // inject import if not exists
        if (!mod.includes("SessionManager")) {
            mod = `import SessionManager from '../utils/SessionManager';\n` + mod;
        }
    }

    if (modified) {
        fs.writeFileSync(filePath, mod, 'utf8');
        console.log(`Patched ${basename}`);
    }
};

const files = walkSync(srcDir);
files.forEach(f => mapPermission(fs.readFileSync(f, 'utf8'), f));
console.log('Frontend patching complete.');

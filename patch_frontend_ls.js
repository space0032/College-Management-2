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

const mapLocalStorage = (fileContent, filePath) => {
    let basename = path.basename(filePath, '.js');
    let mod = fileContent;
    let modified = false;

    // Pattern 1: user
    const patternUser1 = /const user = \(\(\) => \{ try \{ return JSON\.parse\(localStorage\.getItem\('user'\) \|\| '\{\}'\); \} catch \{ return \{.*?\}; \} \}\)\(\);/g;
    if (patternUser1.test(mod)) {
        mod = mod.replace(patternUser1, "const user = SessionManager.getUser() || {};");
        modified = true;
    }

    // Pattern 2: currentUser
    const patternUser2 = /const currentUser = \(\(\) => \{ try \{ return JSON\.parse\(localStorage\.getItem\('user'\) \|\| '\{\}'\); \} catch \{ return \{.*?\}; \} \}\)\(\);/g;
    if (patternUser2.test(mod)) {
        mod = mod.replace(patternUser2, "const currentUser = SessionManager.getUser() || {};");
        modified = true;
    }

    // Pattern 3: userRole
    const patternRole1 = /const userRole = localStorage\.getItem\('userRole'\)( \|\| 'STUDENT')?;/g;
    if (patternRole1.test(mod)) {
        mod = mod.replace(patternRole1, "const userRole = SessionManager.getUserRole() || 'STUDENT';");
        modified = true;
    }

    // Pattern 4: userId / studentId
    const patternId1 = /const (userId|studentId) = parseInt\(localStorage\.getItem\('userId'\) \|\| '1'\);/g;
    if (patternId1.test(mod)) {
        mod = mod.replace(patternId1, "const $1 = SessionManager.getUserId();");
        modified = true;
    }

    if (modified) {
        // inject import if not exists
        if (!mod.includes("SessionManager.get") && !mod.includes("SessionManager from")) {
            // Check depth for relative path
            const depth = filePath.split(path.sep).length - srcDir.split(path.sep).length;
            let rel = '../'.repeat(depth - 1) + 'utils/SessionManager';
            if (depth === 1) rel = './utils/SessionManager';
            mod = `import SessionManager from '${rel}';\n` + mod;
        } else if (!mod.includes("import SessionManager from")) {
            const depth = filePath.split(path.sep).length - srcDir.split(path.sep).length;
            let rel = '../'.repeat(depth - 1) + 'utils/sessionManager'; // Adjust casing if needed
            if (depth === 1) rel = './utils/sessionManager';
            mod = `import SessionManager from '${rel}';\n` + mod;
        }

        fs.writeFileSync(filePath, mod, 'utf8');
        console.log(`Patched ${basename}`);
    }
};

const files = walkSync(srcDir);
files.forEach(f => mapLocalStorage(fs.readFileSync(f, 'utf8'), f));
console.log('Frontend patching complete.');

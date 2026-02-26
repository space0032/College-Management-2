const fs = require('fs');
const path = require('path');

const srcDir = path.join('c:', 'Users', 'antar', 'IdeaProjects', 'College-Management-2', 'src', 'main', 'java', 'com', 'college', 'api');

function inferPermission(className, methodName) {
    const entity = className.replace('Controller', '').toUpperCase();
    if (methodName.startsWith('handleGet') || methodName.startsWith('handleSearch') || methodName.startsWith('handleList')) {
        return `VIEW_${entity}`;
    } else if (methodName.startsWith('handleAdd') || methodName.startsWith('handleCreate') || methodName.startsWith('handlePost')) {
        return `CREATE_${entity}`;
    } else if (methodName.startsWith('handleUpdate') || methodName.startsWith('handleEdit') || methodName.startsWith('handlePut')) {
        return `UPDATE_${entity}`;
    } else if (methodName.startsWith('handleDelete') || methodName.startsWith('handleRemove')) {
        return `DELETE_${entity}`;
    } else {
        return `MANAGE_${entity}`;
    }
}

let count = 0;

fs.readdirSync(srcDir).forEach(filename => {
    if (!filename.endsWith('Controller.java')) return;
    if (['BaseController.java', 'AuthController.java', 'StudentController.java', 'CourseController.java', 'FacultyController.java', 'GradeController.java', 'EventController.java'].includes(filename)) return;

    const filepath = path.join(srcDir, filename);
    let content = fs.readFileSync(filepath, 'utf8');

    // Regex to find: private void handleSomething(HttpExchange t... ) [throws IOException] {
    const pattern = /(private\s+(?:void|\w+)\s+(handle\w+)\s*\([^)]*HttpExchange\s+t[^)]*\)\s*(?:throws\s+IOException)?\s*\{)(?!\s*if\s*\(!requirePermission)/g;

    const newContent = content.replace(pattern, (match, fullMatch, methodName) => {
        if (methodName === 'handleOptions' || methodName === 'handle') return fullMatch;
        const className = filename.replace('.java', '');
        const perm = inferPermission(className, methodName);
        return `${fullMatch}\n        if (!requirePermission(t, "${perm}")) return;`;
    });

    if (newContent !== content) {
        fs.writeFileSync(filepath, newContent, 'utf8');
        count++;
        console.log(`Patched ${filename}`);
    }
});

console.log(`Total files patched: ${count}`);

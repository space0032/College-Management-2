import os
import re

src_dir = r"c:\Users\antar\IdeaProjects\College-Management-2\src\main\java\com\college\api"

# Mapping rough entity names or method types to permissions
def infer_permission(class_name, method_name):
    entity = class_name.replace("Controller", "").upper()
    if method_name.startswith("handleGet") or method_name.startswith("handleSearch") or method_name.startswith("handleList"):
        return f"VIEW_{entity}"
    elif method_name.startswith("handleAdd") or method_name.startswith("handleCreate") or method_name.startswith("handlePost"):
        return f"CREATE_{entity}"
    elif method_name.startswith("handleUpdate") or method_name.startswith("handleEdit") or method_name.startswith("handlePut"):
        return f"UPDATE_{entity}"
    elif method_name.startswith("handleDelete") or method_name.startswith("handleRemove"):
        return f"DELETE_{entity}"
    else:
        # Default fallback
        return f"MANAGE_{entity}"

count = 0

for filename in os.listdir(src_dir):
    if not filename.endswith("Controller.java"): continue
    if filename in ["BaseController.java", "AuthController.java", "StudentController.java", "CourseController.java", "FacultyController.java", "GradeController.java", "EventController.java"]:
        continue
    
    filepath = os.path.join(src_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # We want to match: private void handleSomething(HttpExchange t, ...) { ... try {
    # Or just inject right after the method signature opening brace.
    
    # Regex to find: private void handleSomething(HttpExchange t... ) throws IOException {
    pattern = r'(private\s+(?:void|\w+)\s+(handle\w+)\s*\([^)]*HttpExchange\s+t[^)]*\)\s*(?:throws\s+IOException)?\s*\{)'
    
    def replacer(match):
        full_match = match.group(1)
        method_name = match.group(2)
        if method_name == "handleOptions" or method_name == "handle":
            return full_match
        
        class_name = filename.replace(".java", "")
        perm = infer_permission(class_name, method_name)
        
        # Check if it already has requirePermission to avoid double injection
        # This script runs once so it should be fine.
        return full_match + f'\n        if (!requirePermission(t, "{perm}")) return;'

    new_content = re.sub(pattern, replacer, content)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        count += 1
        print(f"Patched {filename}")

print(f"Total files patched: {count}")

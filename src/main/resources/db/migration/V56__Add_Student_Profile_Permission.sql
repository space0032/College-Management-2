-- Add VIEW_STUDENT_PROFILE permission and grant to ADMIN, FACULTY, STUDENT

-- 1. Register the new permission
INSERT INTO permissions (code, name, category) VALUES
('VIEW_STUDENT_PROFILE', 'View Student Profile', 'Student')
ON CONFLICT (code) DO NOTHING;

-- 2. Grant to ADMIN (superuser - all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Grant to FACULTY
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'VIEW_STUDENT_PROFILE'
WHERE r.code = 'FACULTY' ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 4. Grant to STUDENT (so students can view their own profile)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'VIEW_STUDENT_PROFILE'
WHERE r.code = 'STUDENT' ON CONFLICT (role_id, permission_id) DO NOTHING;
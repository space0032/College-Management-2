-- Ensure MANAGE_TIMETABLE permission exists in permissions table
-- This permission controls who can edit the weekly timetable

INSERT INTO permissions (code, name, category, description)
SELECT 'MANAGE_TIMETABLE', 'Manage Timetable', 'Academic', 
       'Ability to create and edit weekly timetable entries for departments and semesters'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'MANAGE_TIMETABLE'
);

-- Grant this permission to Faculty and Admin roles by default
SET @perm_id = (SELECT id FROM permissions WHERE code = 'MANAGE_TIMETABLE' LIMIT 1);
SET @faculty_role_id = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);

-- Faculty gets timetable management
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @perm_id
WHERE @faculty_role_id IS NOT NULL AND @perm_id IS NOT NULL;

-- Admin gets timetable management
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @perm_id
WHERE @admin_role_id IS NOT NULL AND @perm_id IS NOT NULL;

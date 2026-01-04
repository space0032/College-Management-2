-- Ensure Timetable Permissions
-- Give FACULTY role the ability to MANAGE_TIMETABLE

SET @faculty_id = (SELECT id FROM roles WHERE code = 'FACULTY');
SET @perm_id = (SELECT id FROM permissions WHERE code = 'MANAGE_TIMETABLE');

-- Insert if not exists
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
VALUES (@faculty_id, @perm_id);

-- Ensure MANAGE_ASSIGNMENTS permission exists (in case V9 was already applied without it)
INSERT IGNORE INTO permissions (code, name, category, description) VALUES 
('MANAGE_ASSIGNMENTS', 'Manage Assignments', 'ACADEMIC', 'Create/Grade assignments');

-- Assign to ADMIN
SET @admin_id = (SELECT id FROM roles WHERE code = 'ADMIN');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @admin_id, id FROM permissions WHERE code = 'MANAGE_ASSIGNMENTS';

-- Assign to FACULTY
SET @faculty_id = (SELECT id FROM roles WHERE code = 'FACULTY');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @faculty_id, id FROM permissions WHERE code = 'MANAGE_ASSIGNMENTS';

-- User mentioned "give that role legacy to add and remove assignments"
-- If they meant a specific role "LEGACY", we can create it. 
-- But likely they meant "give [FACULTY] role the legacy/privilege"
-- We will stick to ADMIN and FACULTY for now.

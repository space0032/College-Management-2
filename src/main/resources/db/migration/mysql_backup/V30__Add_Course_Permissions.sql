-- Add Course Management permissions missing in V9
INSERT IGNORE INTO permissions (code, name, category, description) VALUES 
('MANAGE_ALL_COURSES', 'Manage All Courses', 'ACADEMIC', 'Add/Edit/Delete any course'),
('MANAGE_OWN_COURSES', 'Manage Own Courses', 'ACADEMIC', 'Manage assigned courses');

-- Assign MANAGE_ALL_COURSES to ADMIN
SET @admin_id = (SELECT id FROM roles WHERE code = 'ADMIN');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @admin_id, id FROM permissions WHERE code = 'MANAGE_ALL_COURSES';

-- Assign MANAGE_OWN_COURSES to FACULTY
SET @faculty_id = (SELECT id FROM roles WHERE code = 'FACULTY');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @faculty_id, id FROM permissions WHERE code = 'MANAGE_OWN_COURSES';

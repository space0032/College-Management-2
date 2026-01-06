-- Remove Staff role if it exists (assuming it was just a placeholder)
DELETE FROM roles WHERE code = 'STAFF' AND is_system_role = FALSE;

-- Insert Special Roles
INSERT IGNORE INTO roles (code, name, description, is_system_role) VALUES 
('FINANCE', 'Finance Manager', 'Finance and Fee Management', TRUE),
('EXAM_COORD', 'Exam Coordinator', 'Examination Management', TRUE);

-- Ensure Permissions Exist
INSERT IGNORE INTO permissions (name, code, category) VALUES 
('View All Fees', 'VIEW_ALL_FEES', 'Fees'),
('Manage Fees', 'MANAGE_FEES', 'Fees'),
('View Grades', 'VIEW_GRADES', 'Academics'),
('Manage Grades', 'MANAGE_GRADES', 'Academics');

-- Assign Permissions to Roles
-- Finance Manager: Manage Fees, View All Fees
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'FINANCE' AND p.code IN ('MANAGE_FEES', 'VIEW_ALL_FEES');

-- Exam Coordinator: Manage Grades, View Grades
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'EXAM_COORD' AND p.code IN ('MANAGE_GRADES', 'VIEW_GRADES');


-- Update descriptions for clarity
UPDATE roles SET name = 'Administrator' WHERE code = 'ADMIN';

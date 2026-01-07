-- V36 Add Payroll Permission
INSERT INTO permissions (code, name, description, category) 
VALUES ('MANAGE_PAYROLL', 'Manage Payroll', 'Manage Payroll Generation and Processing', 'Finance')
ON CONFLICT (code) DO NOTHING;

-- Assign to ADMIN and FINANCE roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code IN ('ADMIN', 'FINANCE') AND p.code = 'MANAGE_PAYROLL'
ON CONFLICT DO NOTHING;

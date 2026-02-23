-- Add MANAGE_ROOMS permission
INSERT INTO permissions (name, code, description, category) 
VALUES ('Manage Rooms', 'MANAGE_ROOMS', 'Add, edit, and delete room information', 'SYSTEM')
ON CONFLICT (code) DO UPDATE SET description = EXCLUDED.description;

-- Grant to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'ADMIN' AND p.code = 'MANAGE_ROOMS'
ON CONFLICT (role_id, permission_id) DO NOTHING;

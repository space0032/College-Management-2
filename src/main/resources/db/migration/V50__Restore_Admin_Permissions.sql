-- Keep the system ADMIN role linked to every permission currently registered.
-- PermissionService also treats ADMIN as a superuser so future controller
-- permissions cannot accidentally lock out the administrator.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

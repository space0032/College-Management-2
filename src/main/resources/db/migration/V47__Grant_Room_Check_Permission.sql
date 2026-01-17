-- Grant ROOM_CHECK permission to appropriate roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code IN ('ADMIN', 'FACULTY') 
AND p.code = 'ROOM_CHECK'
ON CONFLICT DO NOTHING;

-- Grant MANAGE_ROOMS permission to FACULTY (Admin already has it from V46)
-- Wardens do not manage academic rooms.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'FACULTY' 
AND p.code = 'MANAGE_ROOMS'
ON CONFLICT DO NOTHING;

-- V70: Grant gate-pass permissions to the WARDEN role.
--
-- Wardens approve and reject resident gate passes, but neither the API
-- controller nor earlier migrations ever granted them the VIEW_GATEPASS /
-- MANAGE_GATEPASS permissions, so every warden gate-pass request returned 403.
-- Also grant the same rows to ADMIN (superuser) for consistency.

-- 1. Ensure the permissions exist (idempotent).
INSERT INTO permissions (code, name, category) VALUES
('VIEW_GATEPASS', 'View Gate Passes', 'Gate Pass'),
('MANAGE_GATEPASS', 'Manage Gate Passes', 'Gate Pass'),
('APPROVE_GATE_PASS', 'Approve Gate Pass', 'Gate Pass')
ON CONFLICT (code) DO NOTHING;

-- 2. Grant the permissions to the WARDEN role.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('VIEW_GATEPASS', 'MANAGE_GATEPASS', 'APPROVE_GATE_PASS')
WHERE r.code = 'WARDEN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3. Ensure the ADMIN superuser also has them.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('VIEW_GATEPASS', 'MANAGE_GATEPASS', 'APPROVE_GATE_PASS')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
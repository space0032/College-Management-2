-- V71: Ensure the WARDEN role exists and is linked to every warden account.
--
-- Context: V65 deleted every non-ADMIN role; V66 only recreated STUDENT and
-- FACULTY. New wardens were then created with a legacy 'WARDEN' role string but
-- a NULL role_id, and PermissionService only reads users.role_id — so those
-- accounts had zero permissions (gate passes, hostel attendance, etc.).
--
-- This migration:
--   1) Recreates the WARDEN role when missing.
--   2) Assigns the role's standard permissions (idempotent), including gate pass.
--   3) Backfills existing warden users (linked via wardens.user_id or the
--      legacy role string) with the WARDEN role_id.

-- 1) Recreate the WARDEN role if it was removed by V65.
INSERT INTO roles (code, name, description, is_system_role, portal_type)
SELECT 'WARDEN', 'Hostel Warden', 'Manages hostel rooms, gate passes, and resident students', TRUE, 'WARDEN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'WARDEN');

-- 2) Grant the WARDEN role its standard permissions (idempotent).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_HOSTEL', 'CREATE_HOSTEL', 'UPDATE_HOSTEL', 'DELETE_HOSTEL', 'MANAGE_HOSTEL',
    'VIEW_HOSTEL_ATTENDANCE',
    'VIEW_ROOM', 'MANAGE_ROOM', 'ROOM_CHECK',
    'VIEW_STUDENT', 'MANAGE_STUDENTS',
    'VIEW_ANNOUNCEMENT', 'VIEW_NOTIFICATION', 'CREATE_ANNOUNCEMENT',
    'VIEW_LEAVE', 'UPDATE_LEAVE',
    'VIEW_COMPLAINT', 'MANAGE_COMPLAINT',
    'VIEW_GATEPASS', 'MANAGE_GATEPASS', 'APPROVE_GATE_PASS',
    'VIEW_ATTENDANCE_REPORT', 'VIEW_FEES_REPORT'
)
WHERE r.code = 'WARDEN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3a) Backfill: users linked through wardens.user_id get the WARDEN role.
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'WARDEN'), role = 'WARDEN'
WHERE role_id IS NULL
AND id IN (SELECT user_id FROM wardens WHERE user_id IS NOT NULL)
AND EXISTS (SELECT 1 FROM roles WHERE code = 'WARDEN');

-- 3b) Backfill: legacy users whose role string is WARDEN get the role_id too.
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'WARDEN')
WHERE role_id IS NULL
AND role = 'WARDEN'
AND EXISTS (SELECT 1 FROM roles WHERE code = 'WARDEN');
-- V57: Add dedicated Faculty Portal permissions.
--
-- Previously the Faculty Portal (self-service dashboard) was gated by the
-- general VIEW_FACULTY permission, so it could not be controlled independently.
-- This migration registers portal-specific permissions and grants them to the
-- ADMIN (superuser) and FACULTY roles.
--
-- All statements are idempotent (ON CONFLICT ... DO NOTHING).
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) Register the new permission codes.
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, name, category) VALUES
('VIEW_FACULTY_PORTAL',       'View Faculty Portal',       'Faculty'),
('UPDATE_MY_FACULTY_PROFILE', 'Update Own Faculty Profile', 'Faculty')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) Grant to ADMIN (superuser role).
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN' AND p.code IN ('VIEW_FACULTY_PORTAL', 'UPDATE_MY_FACULTY_PROFILE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3) Grant to FACULTY role.
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'FACULTY' AND p.code IN ('VIEW_FACULTY_PORTAL', 'UPDATE_MY_FACULTY_PROFILE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

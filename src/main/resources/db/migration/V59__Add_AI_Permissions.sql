-- V59: Add AI drafting permissions (F1 assignment generator, A2 announcement drafter).
--
-- USE_AI_FACULTY grants access to the assignment-draft feature and is held by
-- ADMIN (superuser) and FACULTY. USE_AI_ADMIN grants access to the
-- announcement-draft feature and is held by ADMIN only (administrators also
-- pass every permission check via the ADMIN bypass in PermissionService).
--
-- All statements are idempotent (ON CONFLICT ... DO NOTHING).
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) Register the new permission codes.
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, name, category) VALUES
('USE_AI_FACULTY', 'Use AI Assignment Generator', 'AI'),
('USE_AI_ADMIN',   'Use AI Announcement Drafter', 'AI')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) Grant to ADMIN (superuser role).
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN' AND p.code IN ('USE_AI_FACULTY', 'USE_AI_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3) Grant assignment-draft access to FACULTY role.
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'FACULTY' AND p.code IN ('USE_AI_FACULTY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

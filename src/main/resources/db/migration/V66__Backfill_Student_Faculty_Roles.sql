-- V66: Auto-assign STUDENT / FACULTY roles to existing users.
--
-- Context: V65 deleted all non-ADMIN roles, which SET users.role_id NULL.
-- After recreating the STUDENT and FACULTY roles, this backfill re-links
-- every existing user to the correct role:
--   1) Users linked in students.user_id  -> STUDENT role
--   2) Users linked in faculty.user_id   -> FACULTY role
--   3) Fallback: legacy users.role string for users in neither table
--
-- Safety guarantees (all statements are idempotent):
--   * Only rows with role_id IS NULL are touched: ADMIN accounts and any
--     already-assigned user are never modified.
--   * Each statement is guarded by EXISTS on the role code, so it is a
--     harmless no-op if that role has not been (re)created yet.
--   * Both users.role_id and the legacy users.role string are synced.
--   * A user linked in BOTH students and faculty tables gets STUDENT
--     (statement 1 runs first; statement 2 then skips them).
--
-- NOTE: MigrationRunner splits on semicolons, so no DO blocks are used.
-- ============================================================================

-- 1) All students -> STUDENT role
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'STUDENT'), role = 'STUDENT'
WHERE role_id IS NULL
AND id IN (SELECT user_id FROM students WHERE user_id IS NOT NULL)
AND EXISTS (SELECT 1 FROM roles WHERE code = 'STUDENT');

-- 2) All faculty -> FACULTY role
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'FACULTY'), role = 'FACULTY'
WHERE role_id IS NULL
AND id IN (SELECT user_id FROM faculty WHERE user_id IS NOT NULL)
AND EXISTS (SELECT 1 FROM roles WHERE code = 'FACULTY');

-- 3) Fallback: legacy role string for users in neither linkage table
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = users.role)
WHERE role_id IS NULL
AND users.role IN ('STUDENT', 'FACULTY')
AND EXISTS (SELECT 1 FROM roles r WHERE r.code = users.role);

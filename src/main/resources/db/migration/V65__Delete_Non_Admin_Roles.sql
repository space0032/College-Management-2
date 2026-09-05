-- V65: Delete all roles except ADMIN so roles/permissions can be rebuilt from scratch.
--
-- Effects:
--   * role_permissions rows for deleted roles disappear automatically
--     (role_permissions.role_id is ON DELETE CASCADE).
--   * users that belonged to a deleted role keep their accounts but get
--     users.role_id SET NULL (ON DELETE SET NULL), so they lose all
--     permissions until they are reassigned to a recreated role.
--   * The ADMIN role and its permissions are untouched.
--   * Recreated roles start with ZERO permissions; grant them explicitly
--     via Institute Management -> Permission Tree.
-- ============================================================================

DELETE FROM roles WHERE code <> 'ADMIN';

-- Fix Admin User Role Linkage
-- Ensure the 'admin' user is linked to the 'ADMIN' role via role_id

SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);

-- Update admin user
UPDATE users 
SET role_id = @admin_role_id 
WHERE username = 'admin' AND (role_id IS NULL OR role_id != @admin_role_id);

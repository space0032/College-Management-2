-- Add membership approval system to clubs

-- Add status column to club_memberships
ALTER TABLE club_memberships 
ADD COLUMN status VARCHAR(20) DEFAULT 'APPROVED' AFTER role;

-- Update existing memberships to be approved
UPDATE club_memberships SET status = 'APPROVED' WHERE status IS NULL;

-- Add index for status queries
ALTER TABLE club_memberships 
ADD INDEX idx_status (status);

-- Add APPROVE_CLUB_MEMBERS permission
INSERT INTO permissions (code, name, category, description)
SELECT 'APPROVE_CLUB_MEMBERS', 'Approve Club Members', 'Student Management', 'Approve or reject club membership requests'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'APPROVE_CLUB_MEMBERS');

-- Assign to Admin and Faculty
SET @approve_members_id = (SELECT id FROM permissions WHERE code = 'APPROVE_CLUB_MEMBERS' LIMIT 1);
SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);
SET @faculty_role_id = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @approve_members_id WHERE @admin_role_id IS NOT NULL AND @approve_members_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @approve_members_id WHERE @faculty_role_id IS NOT NULL AND @approve_members_id IS NOT NULL;

-- Add portal_type to roles
ALTER TABLE roles ADD COLUMN portal_type VARCHAR(20) DEFAULT 'STUDENT';

-- Update existing roles
UPDATE roles SET portal_type = 'ADMIN' WHERE code = 'ADMIN';
UPDATE roles SET portal_type = 'FACULTY' WHERE code = 'FACULTY';
UPDATE roles SET portal_type = 'STUDENT' WHERE code = 'STUDENT';
UPDATE roles SET portal_type = 'WARDEN' WHERE code = 'WARDEN';
UPDATE roles SET portal_type = 'FINANCE' WHERE code IN ('FINANCE', 'ACCOUNTANT');

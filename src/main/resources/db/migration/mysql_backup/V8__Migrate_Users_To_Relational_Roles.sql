-- Add role_id to users table
ALTER TABLE users ADD COLUMN role_id INT;

-- Ensure standard roles exist
INSERT IGNORE INTO roles (code, name, description, is_system_role) VALUES 
('ADMIN', 'Administrator', 'Full system access', TRUE),
('FACULTY', 'Faculty', 'Academic access', TRUE),
('STUDENT', 'Student', 'Student access', TRUE),
('STAFF', 'Staff', 'Administrative staff', FALSE);

-- Update users with appropriate role_id based on the old role string
UPDATE users u 
JOIN roles r ON u.role = r.code 
SET u.role_id = r.id;

-- Fallback for case sensitivity or slight mismatches if necessary
UPDATE users u 
JOIN roles r ON UPPER(u.role) = r.code 
SET u.role_id = r.id 
WHERE u.role_id IS NULL;

-- Default any remaining NULL role_ids to STUDENT (safety net) or handle as needed
-- (Optional: For now we leave them NULL if they don't match, or maybe create a guest role?)

-- Add Foreign Key Constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL;

-- (Optional) Drop the old role column if we are confident. 
-- However, for safety, let's keep it but make it nullable/deprecated for now, 
-- or drop it if we are sure the code doesn't use it anymore.
-- existing code in UserDAO might still reference 'role'.
-- Let's check UserDAO first? 
-- But existing error is explicitly about missing 'role_id'.
-- We will leave the 'role' column for now to avoid breaking other parts of the code that might read it.

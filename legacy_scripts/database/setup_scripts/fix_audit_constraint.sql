-- Fix Audit Log Foreign Key Constraint
USE college_management;

-- Drop the foreign key constraint that's causing issues
ALTER TABLE audit_logs DROP FOREIGN KEY audit_logs_ibfk_1;

-- Make user_id nullable
ALTER TABLE audit_logs MODIFY COLUMN user_id INT NULL;

-- Re-add the foreign key constraint, but allow NULL values
ALTER TABLE audit_logs 
ADD CONSTRAINT audit_logs_ibfk_1 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE;

SELECT 'Audit log foreign key constraint fixed!' as Status;

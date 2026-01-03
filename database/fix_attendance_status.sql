-- Fix attendance status column to support LATE status
-- This fixes the "Data truncated for column 'status'" error

USE college_management;

-- Update the status column to include 'LATE'
ALTER TABLE attendance 
MODIFY COLUMN status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL;

-- Verify the change
DESCRIBE attendance;

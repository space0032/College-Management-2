-- Add missing 'remarks' column to attendance table
-- This column is used by AttendanceDAO.markBulkAttendance()

ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS remarks VARCHAR(500) DEFAULT NULL AFTER status;

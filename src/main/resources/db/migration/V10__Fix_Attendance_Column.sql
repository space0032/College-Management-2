-- V10: Fix Attendance Table
-- Add missing 'marked_by' column which is used by AttendanceDAO

ALTER TABLE attendance ADD COLUMN IF NOT EXISTS marked_by INTEGER REFERENCES users(id) ON DELETE SET NULL;

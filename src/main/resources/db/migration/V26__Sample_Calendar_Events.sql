-- Note: This migration is intentionally left minimal to avoid conflicts.
-- Use the admin interface to add departments, faculty, and students manually,
-- or populate via the application's import features.

-- The realistic seed data has been documented in credentials.txt
-- Manual data entry recommended for production scenarios.

-- Add a few sample calendar events
INSERT INTO calendar_events (title, event_date, event_type, description) VALUES
('Republic Day', '2026-01-26', 'HOLIDAY', 'National Holiday - Republic Day of India'),
('Mid-Semester Exams', '2026-02-15', 'EXAM', 'Mid-semester examinations'),
('Annual Tech Fest', '2026-03-20', 'EVENT', 'College Annual Technical Festival'),
('Final Exams Begin', '2026-05-01', 'EXAM', 'End semester examinations');

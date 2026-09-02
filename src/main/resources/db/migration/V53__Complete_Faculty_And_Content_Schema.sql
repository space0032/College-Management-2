-- Fields referenced by FacultyDAO but absent from the original PostgreSQL schema.
ALTER TABLE faculty ADD COLUMN IF NOT EXISTS specialization VARCHAR(255);

-- Keep content sequences safe when environments contain explicit-ID seed records.
SELECT setval(pg_get_serial_sequence('assignments', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM assignments;
SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM events;
SELECT setval(pg_get_serial_sequence('timetable', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM timetable;
SELECT setval(pg_get_serial_sequence('clubs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM clubs;
SELECT setval(pg_get_serial_sequence('learning_resources', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM learning_resources;
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM roles;

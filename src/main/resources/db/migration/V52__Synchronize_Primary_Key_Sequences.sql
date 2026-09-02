-- Supabase data may have been inserted with explicit IDs during bootstrap or QA.
-- Move each PostgreSQL sequence to the current table maximum before the next insert.
SELECT setval(pg_get_serial_sequence('departments', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM departments;
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM roles;
SELECT setval(pg_get_serial_sequence('permissions', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM permissions;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM users;
SELECT setval(pg_get_serial_sequence('students', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM students;
SELECT setval(pg_get_serial_sequence('faculty', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM faculty;
SELECT setval(pg_get_serial_sequence('courses', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM courses;
SELECT setval(pg_get_serial_sequence('employees', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM employees;
SELECT setval(pg_get_serial_sequence('books', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM books;
SELECT setval(pg_get_serial_sequence('hostels', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM hostels;
SELECT setval(pg_get_serial_sequence('rooms', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM rooms;
SELECT setval(pg_get_serial_sequence('hostel_allocations', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM hostel_allocations;
SELECT setval(pg_get_serial_sequence('announcements', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM announcements;
SELECT setval(pg_get_serial_sequence('notifications', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM notifications;
SELECT setval(pg_get_serial_sequence('placement_companies', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM placement_companies;
SELECT setval(pg_get_serial_sequence('placement_drives', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM placement_drives;
SELECT setval(pg_get_serial_sequence('visitors', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM visitors;
SELECT setval(pg_get_serial_sequence('visitor_logs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM visitor_logs;
SELECT setval(pg_get_serial_sequence('audit_logs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM audit_logs;

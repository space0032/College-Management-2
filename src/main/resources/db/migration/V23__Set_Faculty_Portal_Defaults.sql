-- Set specific roles to use FACULTY portal view
-- As requested: Everyone uses Faculty view except Admin, Student, Finance, Warden

-- HOD, Librarian, Exam Coordinator -> FACULTY View
UPDATE roles SET portal_type = 'FACULTY' WHERE code IN ('HOD', 'LIBRARIAN', 'EXAM_COORD');

-- Ensure exceptions are correct (redundant but safe)
UPDATE roles SET portal_type = 'ADMIN' WHERE code = 'ADMIN';
UPDATE roles SET portal_type = 'STUDENT' WHERE code = 'STUDENT';
UPDATE roles SET portal_type = 'FINANCE' WHERE code = 'FINANCE';
UPDATE roles SET portal_type = 'WARDEN' WHERE code = 'WARDEN';

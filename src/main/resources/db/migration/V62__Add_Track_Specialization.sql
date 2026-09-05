-- Track-inside-department foundation (Option A).
-- Subjects remain rows in courses; track stored as specialization (free text, e.g. 'Cyber Security').
-- Students carry their track in students.specialization.
-- Timetable becomes track-aware via specialization + optional course_id link.

-- Courses: ensure classification columns exist (safe if already added by earlier migrations)
ALTER TABLE courses ADD COLUMN IF NOT EXISTS specialization VARCHAR(100);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS course_type VARCHAR(50) DEFAULT 'CORE';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS capacity INT DEFAULT 60;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS enrolled_count INT DEFAULT 0;

-- Students: track inside department (e.g. department='Computer Engineering', specialization='Cyber Security')
ALTER TABLE students ADD COLUMN IF NOT EXISTS specialization VARCHAR(100);

-- Timetable: track-aware + optional link to subject row
ALTER TABLE timetable ADD COLUMN IF NOT EXISTS specialization VARCHAR(100);
ALTER TABLE timetable ADD COLUMN IF NOT EXISTS course_id INT;

-- Helpful indexes (PostgreSQL supports IF NOT EXISTS for indexes)
CREATE INDEX IF NOT EXISTS idx_courses_dept_sem_spec ON courses (department, semester, specialization);
CREATE INDEX IF NOT EXISTS idx_courses_type ON courses (course_type);
CREATE INDEX IF NOT EXISTS idx_students_dept_sem_spec ON students (department, semester, specialization);
CREATE INDEX IF NOT EXISTS idx_timetable_dept_sem_spec ON timetable (department, semester, specialization);

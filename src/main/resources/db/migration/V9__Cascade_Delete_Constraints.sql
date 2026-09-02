-- V9: Enable Cascade Delete for Student Relations
-- This allows deleting a student to automatically remove their related records

-- 1. Book Issues
ALTER TABLE book_issues DROP CONSTRAINT IF EXISTS book_issues_student_id_fkey;
ALTER TABLE book_issues ADD CONSTRAINT book_issues_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- Student fees are introduced later by V49 and are created with ON DELETE
-- CASCADE there. Altering them here made fresh PostgreSQL deployments fail
-- because the relation did not exist yet.

-- 2. Student Courses
ALTER TABLE student_courses DROP CONSTRAINT IF EXISTS student_courses_student_id_fkey;
ALTER TABLE student_courses ADD CONSTRAINT student_courses_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 3. Attendance
ALTER TABLE attendance DROP CONSTRAINT IF EXISTS attendance_student_id_fkey;
ALTER TABLE attendance ADD CONSTRAINT attendance_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 4. Hostel Attendance
ALTER TABLE hostel_attendance DROP CONSTRAINT IF EXISTS hostel_attendance_student_id_fkey;
ALTER TABLE hostel_attendance ADD CONSTRAINT hostel_attendance_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 6. Exam Results (Skipped as table does not exist)
-- ALTER TABLE exam_results ...

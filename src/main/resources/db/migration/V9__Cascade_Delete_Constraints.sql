-- V9: Enable Cascade Delete for Student Relations
-- This allows deleting a student to automatically remove their related records

-- 1. Book Issues
ALTER TABLE book_issues DROP CONSTRAINT IF EXISTS book_issues_student_id_fkey;
ALTER TABLE book_issues ADD CONSTRAINT book_issues_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 2. Student Fees
ALTER TABLE student_fees DROP CONSTRAINT IF EXISTS student_fees_student_id_fkey;
ALTER TABLE student_fees ADD CONSTRAINT student_fees_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 3. Student Courses
ALTER TABLE student_courses DROP CONSTRAINT IF EXISTS student_courses_student_id_fkey;
ALTER TABLE student_courses ADD CONSTRAINT student_courses_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 4. Attendance
ALTER TABLE attendance DROP CONSTRAINT IF EXISTS attendance_student_id_fkey;
ALTER TABLE attendance ADD CONSTRAINT attendance_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 5. Hostel Attendance
ALTER TABLE hostel_attendance DROP CONSTRAINT IF EXISTS hostel_attendance_student_id_fkey;
ALTER TABLE hostel_attendance ADD CONSTRAINT hostel_attendance_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- 6. Exam Results (if exists, usually linked to student)
ALTER TABLE exam_results DROP CONSTRAINT IF EXISTS exam_results_student_id_fkey;
ALTER TABLE exam_results ADD CONSTRAINT exam_results_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

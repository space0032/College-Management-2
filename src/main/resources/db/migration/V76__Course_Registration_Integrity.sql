-- V76 Course Registration Integrity
--
-- 1. Dedupe legacy duplicate rows, keeping the most recent registration per
--    student/course. Fixes duplicates created when a rejected request was
--    re-submitted as a brand-new PENDING row instead of reusing the old one.
DELETE FROM course_registrations a
WHERE EXISTS (
    SELECT 1 FROM course_registrations b
    WHERE a.student_id = b.student_id
      AND a.course_id = b.course_id
      AND b.id > a.id
);

-- 2. Enforce at most one registration (any status) per student/course.
CREATE UNIQUE INDEX IF NOT EXISTS uq_course_registrations_student_course
    ON course_registrations (student_id, course_id);
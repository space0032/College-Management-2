-- V78 Scholarship Application Uniqueness (TOCTOU fix)
--
-- A student may only ever have ONE application per scholarship. The API
-- pre-checked for duplicates, but a concurrent double-submit both passing that
-- check could insert two rows. Enforce it at the database level too.
--
-- 1. Dedupe legacy duplicates, keeping one application per student/scholarship.
DELETE FROM scholarship_applications a
WHERE a.status != 'APPLIED'
AND EXISTS (
    SELECT 1 FROM scholarship_applications b
    WHERE b.scholarship_id = a.scholarship_id
      AND b.student_id = a.student_id
      AND b.status = 'APPLIED'
);

DELETE FROM scholarship_applications a
USING scholarship_applications b
WHERE a.scholarship_id = b.scholarship_id
  AND a.student_id = b.student_id
  AND a.id > b.id;

-- 2. Enforce at most one application per student/scholarship.
CREATE UNIQUE INDEX IF NOT EXISTS uq_scholarship_applications_student
    ON scholarship_applications (scholarship_id, student_id);
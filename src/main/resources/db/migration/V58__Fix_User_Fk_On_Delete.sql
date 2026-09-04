-- V58: Allow deleting user accounts (faculty/student) by fixing FK constraints.
-- Several tables reference users(id) without an ON DELETE action, which
-- PostgreSQL treats as RESTRICT and blocks DELETE FROM users. This broke
-- faculty deletion (FacultyDAO deletes the linked user row first).
-- Recreate each FK with ON DELETE SET NULL so user deletion nullifies
-- audit/creator references instead of failing.
--
-- NOTE: MigrationRunner splits SQL on ";" so DO blocks cannot be used here.
-- Constraint names follow PostgreSQL's auto-generated convention.

-- 1. student_feedback.faculty_id (was NOT NULL, no ON DELETE)
ALTER TABLE IF EXISTS student_feedback ALTER COLUMN faculty_id DROP NOT NULL;
ALTER TABLE IF EXISTS student_feedback DROP CONSTRAINT IF EXISTS student_feedback_faculty_id_fkey;
ALTER TABLE IF EXISTS student_feedback ADD CONSTRAINT student_feedback_faculty_id_fkey
    FOREIGN KEY (faculty_id) REFERENCES users(id) ON DELETE SET NULL;

-- 2. assignments.created_by (was NOT NULL, no ON DELETE)
ALTER TABLE IF EXISTS assignments ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE IF EXISTS assignments DROP CONSTRAINT IF EXISTS assignments_created_by_fkey;
ALTER TABLE IF EXISTS assignments ADD CONSTRAINT assignments_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 3. events.created_by
ALTER TABLE IF EXISTS events DROP CONSTRAINT IF EXISTS events_created_by_fkey;
ALTER TABLE IF EXISTS events ADD CONSTRAINT events_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 4. announcements.created_by
ALTER TABLE IF EXISTS announcements DROP CONSTRAINT IF EXISTS announcements_created_by_fkey;
ALTER TABLE IF EXISTS announcements ADD CONSTRAINT announcements_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 5. club_announcements.posted_by
ALTER TABLE IF EXISTS club_announcements DROP CONSTRAINT IF EXISTS club_announcements_posted_by_fkey;
ALTER TABLE IF EXISTS club_announcements ADD CONSTRAINT club_announcements_posted_by_fkey
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE SET NULL;

-- 6. course_syllabi.uploaded_by
ALTER TABLE IF EXISTS course_syllabi DROP CONSTRAINT IF EXISTS course_syllabi_uploaded_by_fkey;
ALTER TABLE IF EXISTS course_syllabi ADD CONSTRAINT course_syllabi_uploaded_by_fkey
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;

-- 7. learning_resources.uploaded_by
ALTER TABLE IF EXISTS learning_resources DROP CONSTRAINT IF EXISTS learning_resources_uploaded_by_fkey;
ALTER TABLE IF EXISTS learning_resources ADD CONSTRAINT learning_resources_uploaded_by_fkey
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;

-- 8. audit_logs.user_id
ALTER TABLE IF EXISTS audit_logs DROP CONSTRAINT IF EXISTS audit_logs_user_id_fkey;
ALTER TABLE IF EXISTS audit_logs ADD CONSTRAINT audit_logs_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 9. fee_payments.received_by
ALTER TABLE IF EXISTS fee_payments DROP CONSTRAINT IF EXISTS fee_payments_received_by_fkey;
ALTER TABLE IF EXISTS fee_payments ADD CONSTRAINT fee_payments_received_by_fkey
    FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE SET NULL;

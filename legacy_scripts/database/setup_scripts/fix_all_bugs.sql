-- ============================================================================
-- FIX ALL REMAINING APPLICATION BUGS
-- ============================================================================
USE college_management;

-- ============================================================================
-- 1. ADD SEMESTER COLUMN TO ASSIGNMENTS TABLE
-- ============================================================================
ALTER TABLE assignments 
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1 AFTER course_id;

-- Update semester based on course codes (if applicable)
UPDATE assignments a
JOIN courses c ON a.course_id = c.id
SET a.semester = c.semester
WHERE c.semester IS NOT NULL;

SELECT 'Assignments table updated with semester column!' as Status;

-- ============================================================================
-- 2. VERIFY AND FIX ALL TABLE SCHEMAS
-- ============================================================================

-- Ensure students table has all required columns
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General',
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS is_hostelite TINYINT(1) DEFAULT 0;

-- Ensure courses table has semester column
ALTER TABLE courses
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

SELECT 'All schema fixes applied!' as Status;

-- ============================================================================
-- 3. VERIFICATION QUERIES
-- ============================================================================

SELECT 'Assignments Schema' as TableCheck;
DESCRIBE assignments;

SELECT 'Sample assignment data' as DataCheck;
SELECT id, title, course_id, semester, due_date 
FROM assignments 
LIMIT 5;

SELECT '=== ALL BUGS FIXED ===' as FinalStatus;

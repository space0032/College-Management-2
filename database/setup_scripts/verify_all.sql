-- ============================================================================
-- FINAL VERIFICATION - CHECK ALL DATABASE SCHEMAS
-- ============================================================================
USE college_management;

-- ============================================================================
-- 1. VERIFY ALL CRITICAL TABLES
-- ============================================================================

SELECT '=== CHECKING CRITICAL TABLES ===' as Status;

-- Check users table
SELECT 'Users Table' as TableName, COUNT(*) as Records FROM users;

-- Check students table with all required columns
SELECT 'Students Table' as TableName, COUNT(*) as Records FROM students;
SELECT COUNT(*) as students_with_department FROM students WHERE department IS NOT NULL;
SELECT COUNT(*) as students_with_semester FROM students WHERE semester IS NOT NULL;
SELECT COUNT(*) as hostelites FROM students WHERE is_hostelite = 1;

-- Check student_fees (the app uses this, not fees)
SELECT 'Student Fees Table' as TableName, COUNT(*) as Records FROM student_fees;

-- Check books table with new columns
SELECT 'Books Table' as TableName, COUNT(*) as Records FROM books;
SELECT COUNT(*) as books_with_publisher FROM books WHERE publisher IS NOT NULL;
SELECT COUNT(*) as books_with_category FROM books WHERE category IS NOT NULL;

-- Check assignments table with semester column
SELECT 'Assignments Table' as TableName;
SHOW COLUMNS FROM assignments LIKE 'semester';

-- Check hostel allocations
SELECT 'Hostel Allocations' as TableName, COUNT(*) as Records FROM hostel_allocations;

-- ============================================================================
-- 2. VERIFY DATA INTEGRITY
-- ============================================================================

SELECT '=== DATA INTEGRITY CHECKS ===' as Status;

-- Check for students without fees
SELECT 
    COUNT(DISTINCT s.id) as total_students,
    COUNT(DISTINCT sf.student_id) as students_with_fees,
    COUNT(DISTINCT s.id) - COUNT(DISTINCT sf.student_id) as students_missing_fees
FROM students s
LEFT JOIN student_fees sf ON s.id = sf.student_id;

-- Check for hostelites without room allocations
SELECT 
    COUNT(*) as hostelites,
    (SELECT COUNT(*) FROM hostel_allocations) as allocated,
    COUNT(*) - (SELECT COUNT(*) FROM hostel_allocations) as unallocated
FROM students WHERE is_hostelite = 1;

-- ============================================================================
-- 3. CHECK FOR COMMON ERRORS
-- ============================================================================

SELECT '=== CHECKING FOR POTENTIAL ISSUES ===' as Status;

-- Check for NULL usernames
SELECT 'Users with NULL username' as Issue, COUNT(*) as Count 
FROM users WHERE username IS NULL;

-- Check for duplicate ISBNs in books
SELECT 'Duplicate ISBNs' as Issue, COUNT(*) as Count
FROM (SELECT isbn FROM books GROUP BY isbn HAVING COUNT(*) > 1) as dups;

-- Check for courses without department_id
SELECT 'Courses without department' as Issue, COUNT(*) as Count
FROM courses WHERE department_id IS NULL;

-- ============================================================================
-- 4. FINAL SUMMARY
-- ============================================================================

SELECT '=== FINAL DATABASE SUMMARY ===' as Status;

SELECT 
    'Users' as Item, COUNT(*) as Count FROM users
UNION ALL SELECT 'Students', COUNT(*) FROM students
UNION ALL SELECT 'Faculty', COUNT(*) FROM faculty
UNION ALL SELECT 'Books', COUNT(*) FROM books
UNION ALL SELECT 'Fee Records', COUNT(*) FROM student_fees
UNION ALL SELECT 'Hostel Allocations', COUNT(*) FROM hostel_allocations
UNION ALL SELECT 'Courses', COUNT(*) FROM courses
UNION ALL SELECT 'Timetables', COUNT(*) FROM timetable
UNION ALL SELECT 'Assignments', COUNT(*) FROM assignments;

SELECT '=== VERIFICATION COMPLETE ===' as FinalStatus;

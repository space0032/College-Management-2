-- Database Verification Script
-- Run this to confirm data is loaded correctly

USE college_management;

-- Count all tables
SELECT 'Total Users' as Item, COUNT(*) as Count FROM users
UNION ALL
SELECT 'Total Faculty', COUNT(*) FROM faculty
UNION ALL
SELECT 'Total Students', COUNT(*) FROM students
UNION ALL
SELECT 'Students with user_id', COUNT(*) FROM students WHERE user_id IS NOT NULL;

-- Show sample students
SELECT '--- SAMPLE STUDENTS ---' as Info;
SELECT s.id, s.name, s.email, s.department, s.semester, u.username 
FROM students s 
LEFT JOIN users u ON s.user_id = u.id 
ORDER BY s.department, s.name 
LIMIT 20;

-- Show sample faculty
SELECT '--- SAMPLE FACULTY ---' as Info;
SELECT f.id, f.name, f.email, f.department
FROM faculty f
ORDER BY f.department, f.name
LIMIT 15;

-- Check for any orphaned records
SELECT '--- DATA INTEGRITY CHECK ---' as Info;
SELECT 
  (SELECT COUNT(*) FROM students WHERE user_id IS NULL) as students_without_users,
  (SELECT COUNT(*) FROM faculty WHERE user_id IS NULL) as faculty_without_users;

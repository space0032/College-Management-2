#!/bin/bash
# Verify and fix database - run as: sudo ./verify_db.sh

echo "Checking database structure..."

mysql -e "
USE college_management;

-- Show students table structure
SELECT 'STUDENTS TABLE:' AS '';
DESCRIBE students;

-- Show timetable table structure if exists
SELECT 'TIMETABLE TABLE:' AS '';
SHOW TABLES LIKE 'timetable';
DESCRIBE timetable;

-- Show all tables
SELECT 'ALL TABLES:' AS '';
SHOW TABLES;
"

-- ============================================================================
-- MIGRATE FEES FROM OLD TABLE TO NEW STUDENT_FEES TABLE
-- ============================================================================
USE college_management;

-- Clear existing student_fees records
DELETE FROM student_fees WHERE id > 0;

-- Create fees for all students based on their semester and hostelite status
-- Academic Year: 2025-2026
-- Tuition: ₹50,000, Hostel: ₹15,000, Library: ₹2,000, Sports: ₹3,000, Exam: ₹5,000

INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    1, -- Tuition Fee
    '2025-2026',
    50000.00,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 50000.00 -- Sem 1,2,7,8 paid
        WHEN s.semester IN (3, 4) THEN 25000.00 -- Sem 3,4 partial
        ELSE 0.00 -- Sem 5,6 pending
    END,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 'PAID'
        WHEN s.semester IN (3, 4) THEN 'PARTIAL'
        ELSE 'PENDING'
    END,
    '2026-01-31'
FROM students s;

-- Add Hostel Fee for hostelites
INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    2, -- Hostel Fee
    '2025-2026',
    15000.00,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 15000.00 -- Paid
        WHEN s.semester IN (3, 4) THEN 7500.00 -- Partial
        ELSE 0.00 -- Pending
    END,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 'PAID'
        WHEN s.semester IN (3, 4) THEN 'PARTIAL'
        ELSE 'PENDING'
    END,
    '2026-01-31'
FROM students s
WHERE s.is_hostelite = 1;

-- Add Library Fee for all students
INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    3, -- Library Fee
    '2025-2026',
    2000.00,
    2000.00, -- All paid
    'PAID',
    '2026-01-31'
FROM students s;

-- Add Sports Fee for all students
INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    4, -- Sports Fee
    '2025-2026',
    3000.00,
    3000.00, -- All paid
    'PAID',
    '2026-01-31'
FROM students s;

-- Add Exam Fee for all students
INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    5, -- Exam Fee
    '2025-2026',
    5000.00,
    CASE 
        WHEN s.semester IN (1, 2, 3, 4) THEN 5000.00 -- Paid
        ELSE 0.00 -- Pending for higher semesters
    END,
    CASE 
        WHEN s.semester IN (1, 2, 3, 4) THEN 'PAID'
        ELSE 'PENDING'
    END,
    '2026-01-31'
FROM students s;

-- Add Lab Fee for all students
INSERT INTO student_fees (student_id, category_id, academic_year, total_amount, paid_amount, status, due_date)
SELECT 
    s.id,
    6, -- Lab Fee
    '2025-2026',
    8000.00,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 8000.00 -- Paid
        WHEN s.semester IN (3, 4, 5) THEN 4000.00 -- Partial
        ELSE 0.00 -- Pending
    END,
    CASE 
        WHEN s.semester IN (1, 2, 7, 8) THEN 'PAID'
        WHEN s.semester IN (3, 4, 5) THEN 'PARTIAL'
        ELSE 'PENDING'
    END,
    '2026-01-31'
FROM students s;

-- Verification
SELECT 'Fees Migration Complete!' as Status;

SELECT 
    fc.category_name,
    COUNT(DISTINCT sf.student_id) as students,
    SUM(sf.total_amount) as total_amount,
    SUM(sf.paid_amount) as paid_amount,
    COUNT(CASE WHEN sf.status = 'PAID' THEN 1 END) as fully_paid,
    COUNT(CASE WHEN sf.status = 'PARTIAL' THEN 1 END) as partial,
    COUNT(CASE WHEN sf.status = 'PENDING' THEN 1 END) as pending
FROM student_fees sf
JOIN fee_categories fc ON sf.category_id = fc.id
GROUP BY fc.category_name
ORDER BY fc.id;

SELECT CONCAT('Total Fee Records: ', COUNT(*)) as Summary FROM student_fees;

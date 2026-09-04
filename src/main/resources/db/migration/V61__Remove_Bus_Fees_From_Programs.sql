-- V61: Remove bus fees from department program fee structures
-- Bus fees are no longer part of per-program fee breakdowns
-- Existing student_fees rows are left untouched for history
-- All statements are idempotent

DELETE FROM program_fee_structure
WHERE category_id IN (
    SELECT id FROM fee_categories
    WHERE LOWER(category_name) LIKE '%bus%'
);

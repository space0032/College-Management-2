-- V60: Customizable per-program fee breakdown
-- Adds program_fee_structure so each department or program can define
-- its own amount per fee category and academic year
-- New student enrollments use this table first and fall back to
-- fee_categories base_amount when no row exists
-- All statements are idempotent

CREATE TABLE IF NOT EXISTS program_fee_structure (
    id SERIAL PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    category_id INTEGER NOT NULL REFERENCES fee_categories(id),
    academic_year VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_program_fee_structure
    ON program_fee_structure (department, category_id, academic_year);

-- Seed defaults for current and next academic year from base amounts
-- so existing behaviour is unchanged until an admin customizes a program
INSERT INTO program_fee_structure (department, category_id, academic_year, amount)
SELECT d.name, fc.id, TO_CHAR(CURRENT_DATE, 'YYYY'), fc.base_amount
FROM departments d CROSS JOIN fee_categories fc
ON CONFLICT (department, category_id, academic_year) DO NOTHING;

INSERT INTO program_fee_structure (department, category_id, academic_year, amount)
SELECT d.name, fc.id, CAST(EXTRACT(YEAR FROM CURRENT_DATE) + 1 AS VARCHAR), fc.base_amount
FROM departments d CROSS JOIN fee_categories fc
ON CONFLICT (department, category_id, academic_year) DO NOTHING;

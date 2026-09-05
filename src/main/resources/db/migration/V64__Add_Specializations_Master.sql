-- V64 tracks master table for specializations inside departments
-- Controlled vocabulary for the track dropdowns in student and subject forms
-- Existing free text tracks in courses and students are imported once

CREATE TABLE IF NOT EXISTS specializations (
    id SERIAL PRIMARY KEY,
    department_id INTEGER NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_specializations_dept_name
    ON specializations (department_id, name);

INSERT INTO specializations (department_id, name)
SELECT DISTINCT d.id, TRIM(c.specialization)
FROM courses c
JOIN departments d ON d.name = c.department
WHERE c.specialization IS NOT NULL AND TRIM(c.specialization) <> ''
ON CONFLICT (department_id, name) DO NOTHING;

INSERT INTO specializations (department_id, name)
SELECT DISTINCT d.id, TRIM(s.specialization)
FROM students s
JOIN departments d ON d.name = s.department
WHERE s.specialization IS NOT NULL AND TRIM(s.specialization) <> ''
ON CONFLICT (department_id, name) DO NOTHING;

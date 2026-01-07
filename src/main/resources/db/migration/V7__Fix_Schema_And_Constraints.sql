-- V7: Fix Schema, Add Missing Columns, and Ensure Constraints

-- 1. Fix Book Issues
ALTER TABLE book_issues ADD COLUMN IF NOT EXISTS issued_by INTEGER REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE book_issues ADD COLUMN IF NOT EXISTS returned_to INTEGER REFERENCES users(id) ON DELETE SET NULL;

-- 2. Ensure Timetable Table Exists (PostgreSQL syntax)
CREATE TABLE IF NOT EXISTS timetable (
    id SERIAL PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    subject VARCHAR(100),
    faculty_name VARCHAR(100),
    room_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add Unique Constraint for Upsert
ALTER TABLE timetable DROP CONSTRAINT IF EXISTS unique_timetable_slot;
ALTER TABLE timetable ADD CONSTRAINT unique_timetable_slot UNIQUE (department, semester, day_of_week, time_slot);


-- 3. Ensure Assignments Table Exists
CREATE TABLE IF NOT EXISTS assignments (
    id SERIAL PRIMARY KEY,
    course_id INTEGER NOT NULL REFERENCES courses(id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    due_date TIMESTAMP NOT NULL,
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Ensure Submissions Table Exists
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    assignment_id INTEGER NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    submission_text TEXT,
    file_path VARCHAR(255),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    marks_obtained DECIMAL(5,2),
    feedback TEXT,
    plagiarism_score INTEGER DEFAULT 0
);

-- Add Unique Constraint for Upsert
ALTER TABLE submissions DROP CONSTRAINT IF EXISTS unique_assignment_submission;
ALTER TABLE submissions ADD CONSTRAINT unique_assignment_submission UNIQUE (assignment_id, student_id);


-- 5. Fix Hostel Attendance Constraints
ALTER TABLE hostel_attendance ADD COLUMN IF NOT EXISTS hostel_id INTEGER REFERENCES hostels(id) ON DELETE SET NULL;
ALTER TABLE hostel_attendance DROP CONSTRAINT IF EXISTS unique_hostel_attendance;
ALTER TABLE hostel_attendance ADD CONSTRAINT unique_hostel_attendance UNIQUE (student_id, date);

-- 6. Seed Fee Categories to prevent FK errors
INSERT INTO fee_categories (id, category_name, base_amount, description) VALUES
(1, 'Tuition Fee', 50000.00, 'Semester Tuition Fee'),
(2, 'Hostel Fee', 25000.00, 'Semester Hostel Fee'),
(3, 'Bus Fee', 15000.00, 'Semester Bus/Transport Fee'),
(4, 'Exam Fee', 2000.00, 'Semester Examination Fee')
ON CONFLICT (id) DO NOTHING;

-- 7. Add indexes for performance if they don't exist (IF NOT EXISTS is not standard in all PG versions for INDEX, so wrapping or ignoring error)
--Skipping naive index creation to avoid errors, relying on constraints.

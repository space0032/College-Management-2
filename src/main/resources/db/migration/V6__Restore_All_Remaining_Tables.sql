-- V6 Restore Remaining Tables (Assignments, BookRequests, Payroll, Timetable)

-- 1. Assignments
CREATE TABLE IF NOT EXISTS assignments (
    id SERIAL PRIMARY KEY,
    course_id INTEGER REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    semester INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Submissions
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    assignment_id INTEGER REFERENCES assignments(id) ON DELETE CASCADE,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    submission_text TEXT,
    file_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    plagiarism_score DECIMAL(5,2),
    marks_obtained DECIMAL(5,2),
    feedback TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Book Requests
CREATE TABLE IF NOT EXISTS book_requests (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    book_id INTEGER REFERENCES books(id) ON DELETE CASCADE,
    loan_period_days INTEGER,
    remarks TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Payroll Entries
CREATE TABLE IF NOT EXISTS payroll_entries (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    basic_salary DECIMAL(10,2),
    bonuses DECIMAL(10,2) DEFAULT 0,
    deductions DECIMAL(10,2) DEFAULT 0,
    net_salary DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Timetable (MySQL FIELD() fix required in DAO)
CREATE TABLE IF NOT EXISTS timetable (
    id SERIAL PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    faculty_name VARCHAR(100),
    room_number VARCHAR(50),
    UNIQUE(department, semester, day_of_week, time_slot)
);

-- 6. Recreate Notifications to match DAO
DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    recipient_user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    recipient_contact VARCHAR(100),
    type VARCHAR(50),
    subject VARCHAR(200),
    message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

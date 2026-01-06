-- Consolidated Supabase (PostgreSQL) Schema
-- Replaces all previous MySQL migrations

-- 1. Departments
CREATE TABLE IF NOT EXISTS departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE
);

-- 2. Roles & Permissions (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    portal_type VARCHAR(20) -- ADMIN, FACULTY, STUDENT
);

CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    description TEXT,
    category VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 3. Users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20), -- Legacy column, kept for backward compat
    role_id INTEGER REFERENCES roles(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Initial Core Tables
CREATE TABLE IF NOT EXISTS students (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(50),
    course VARCHAR(50),
    batch VARCHAR(20),
    semester INTEGER,
    enrollment_date DATE,
    address TEXT,
    is_hostelite BOOLEAN DEFAULT FALSE,
    dob DATE,
    gender VARCHAR(20),
    blood_group VARCHAR(10),
    category VARCHAR(50),
    nationality VARCHAR(50),
    father_name VARCHAR(100),
    mother_name VARCHAR(100),
    guardian_contact VARCHAR(20),
    previous_school VARCHAR(200),
    tenth_percentage DECIMAL(5,2),
    twelfth_percentage DECIMAL(5,2),
    extracurricular_activities TEXT,
    profile_photo_path VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS faculty (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(50),
    designation VARCHAR(50),
    joining_date DATE
);

-- 5. Academics
CREATE TABLE IF NOT EXISTS courses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    credits INTEGER DEFAULT 3,
    department_id INTEGER REFERENCES departments(id) ON DELETE SET NULL,
    semester INTEGER,
    faculty_id INTEGER REFERENCES faculty(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS student_courses (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    course_id INTEGER REFERENCES courses(id) ON DELETE CASCADE,
    semester INTEGER,
    academic_year INTEGER,
    status VARCHAR(20) DEFAULT 'ENROLLED'
);

CREATE TABLE IF NOT EXISTS attendance (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    course_id INTEGER REFERENCES courses(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL, -- PRESENT, ABSENT
    remarks TEXT
);

CREATE TABLE IF NOT EXISTS grades (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    course_id INTEGER REFERENCES courses(id) ON DELETE CASCADE,
    exam_type VARCHAR(50),
    marks_obtained DECIMAL(5,2),
    max_marks DECIMAL(5,2),
    grade VARCHAR(5),
    remarks TEXT
);

-- 6. Fees
CREATE TABLE IF NOT EXISTS fee_categories (
    id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    base_amount DECIMAL(10,2) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS fees (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES fee_categories(id),
    amount DECIMAL(10,2) NOT NULL,
    paid DECIMAL(10,2) DEFAULT 0.00,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS fee_ledger (
    id SERIAL PRIMARY KEY,
    fee_id INTEGER REFERENCES fees(id) ON DELETE CASCADE,
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_date DATE DEFAULT CURRENT_DATE,
    payment_mode VARCHAR(50),
    transaction_id VARCHAR(100),
    remarks TEXT
);

-- 7. Library
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    quantity INTEGER DEFAULT 0,
    available INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS book_issues (
    id SERIAL PRIMARY KEY,
    book_id INTEGER REFERENCES books(id),
    student_id INTEGER REFERENCES students(id),
    issue_date DATE DEFAULT CURRENT_DATE,
    due_date DATE,
    return_date DATE,
    status VARCHAR(20) DEFAULT 'ISSUED',
    fine_amount DECIMAL(10,2) DEFAULT 0.00
);

-- 8. Hostel Management
CREATE TABLE IF NOT EXISTS hostels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL, -- BOYS/GIRLS
    warden_name VARCHAR(100),
    warden_contact VARCHAR(20),
    total_rooms INTEGER DEFAULT 0,
    total_capacity INTEGER DEFAULT 0,
    address TEXT
);

CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    hostel_id INTEGER NOT NULL REFERENCES hostels(id) ON DELETE CASCADE,
    room_number VARCHAR(20) NOT NULL,
    floor INTEGER NOT NULL,
    capacity INTEGER NOT NULL,
    occupied_count INTEGER DEFAULT 0,
    room_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    UNIQUE (hostel_id, room_number)
);

CREATE TABLE IF NOT EXISTS hostel_allocations (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    room_id INTEGER NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    remarks TEXT,
    allocated_by INTEGER REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS hostel_attendance (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL, -- PRESENT, ABSENT, LEAVE
    marked_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gate_passes (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason TEXT NOT NULL,
    destination VARCHAR(100),
    parent_contact VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMP,
    approval_comment TEXT
);

-- 9. Events & Clubs
CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    location VARCHAR(200),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    max_participants INTEGER,
    registration_deadline TIMESTAMP,
    created_by INTEGER REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'UPCOMING',
    banner_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS event_registrations (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attendance_status VARCHAR(20) DEFAULT 'REGISTERED',
    UNIQUE (event_id, student_id)
);

CREATE TABLE IF NOT EXISTS clubs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    logo_path VARCHAR(500),
    president_student_id INTEGER REFERENCES students(id) ON DELETE SET NULL,
    faculty_coordinator_id INTEGER REFERENCES faculty(id) ON DELETE SET NULL,
    member_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS club_memberships (
    id SERIAL PRIMARY KEY,
    club_id INTEGER NOT NULL REFERENCES clubs(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (club_id, student_id)
);

CREATE TABLE IF NOT EXISTS club_announcements (
    id SERIAL PRIMARY KEY,
    club_id INTEGER NOT NULL REFERENCES clubs(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    posted_by INTEGER REFERENCES users(id),
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Advanced Event Features
CREATE TABLE IF NOT EXISTS event_collaborators (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    department_id INTEGER NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'PENDING',
    UNIQUE (event_id, department_id)
);

CREATE TABLE IF NOT EXISTS event_resources (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    resource_name VARCHAR(100) NOT NULL,
    quantity INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'REQUESTED'
);

CREATE TABLE IF NOT EXISTS event_volunteers (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    task_description VARCHAR(200),
    status VARCHAR(20) DEFAULT 'REGISTERED',
    hours_logged FLOAT DEFAULT 0.0,
    UNIQUE (event_id, student_id)
);

-- 11. Learning Resources (Syllabus & Uploads)
CREATE TABLE IF NOT EXISTS resource_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course_syllabi (
    id SERIAL PRIMARY KEY,
    course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    version VARCHAR(50) DEFAULT '1.0',
    description TEXT,
    uploaded_by INTEGER REFERENCES users(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learning_resources (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_id INTEGER REFERENCES courses(id) ON DELETE SET NULL,
    category_id INTEGER REFERENCES resource_categories(id),
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_by INTEGER REFERENCES users(id),
    download_count INTEGER DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resource_tags (
    id SERIAL PRIMARY KEY,
    resource_id INTEGER NOT NULL REFERENCES learning_resources(id) ON DELETE CASCADE,
    tag_name VARCHAR(50) NOT NULL
);

-- 12. Feedback & Logs
CREATE TABLE IF NOT EXISTS student_feedback (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    faculty_id INTEGER NOT NULL REFERENCES users(id),
    feedback_text TEXT NOT NULL,
    category VARCHAR(50),
    is_private BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INTEGER,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS announcements (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    target_audience VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- SEED DATA --

-- Default Roles
INSERT INTO roles (code, name, is_system_role, portal_type) VALUES 
('ADMIN', 'Administrator', TRUE, 'ADMIN'),
('FACULTY', 'Faculty Member', TRUE, 'FACULTY'),
('STUDENT', 'Student', TRUE, 'STUDENT'),
('WARDEN', 'Hostel Warden', TRUE, 'WARDEN'),
('FINANCE', 'Finance Officer', TRUE, 'FINANCE')
ON CONFLICT (code) DO NOTHING;

-- Default User (admin/admin123)
-- Password hash for 'admin123' (assuming simple hash or compatible gen)
INSERT INTO users (username, password, role, role_id) 
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', id FROM roles WHERE code='ADMIN'
ON CONFLICT (username) DO NOTHING;

-- Resource Categories
INSERT INTO resource_categories (name, description) VALUES 
('Lecture Notes', 'Class notes and slides'),
('Lab Manual', 'Laboratory manuals and procedures'),
('Assignment', 'Assignment questions and guides'),
('Past Paper', 'Previous year question papers'),
('Reference Material', 'Additional reading and reference books'),
('Tutorial', 'Tutorial sheets and problems')
ON CONFLICT (name) DO NOTHING;

-- Permissions (Basic set, simplified)
INSERT INTO permissions (code, name, category) VALUES 
('MANAGE_USERS', 'Manage Users', 'Admin'),
('MANAGE_COURSES', 'Manage Courses', 'Academic'),
('MANAGE_EVENTS', 'Manage Events', 'Student Management'),
('VIEW_EVENTS', 'View Events', 'Student Management'),
('MANAGE_CLUBS', 'Manage Clubs', 'Student Management'),
('JOIN_CLUBS', 'Join Clubs', 'Student Management'),
('UPLOAD_RESOURCES', 'Upload Resources', 'Academic'),
('VIEW_RESOURCES', 'View Resources', 'Academic'),
('MANAGE_FEES', 'Manage Fees', 'Finance'),
('PAY_FEES', 'Pay Fees', 'Student'),
('MANAGE_HOSTEL', 'Manage Hostel', 'Hostel'),
('MANAGE_SYSTEM', 'Manage System', 'Admin')
ON CONFLICT (code) DO NOTHING;

-- Assign ALL permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Create database and user for College Management System
-- Run this with: sudo mysql < database/setup_user.sql

-- Create database
CREATE DATABASE IF NOT EXISTS college_management;
USE college_management;

-- Create tables
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'FACULTY', 'STUDENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    course VARCHAR(100),
    batch VARCHAR(20),
    enrollment_date DATE,
    address TEXT,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS faculty (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    department VARCHAR(100),
    qualification VARCHAR(100),
    join_date DATE,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    credits INT,
    department VARCHAR(100),
    semester INT
);

CREATE TABLE IF NOT EXISTS attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT') NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (student_id, course_id, date)
);

CREATE TABLE IF NOT EXISTS grades (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    marks DECIMAL(5,2),
    grade VARCHAR(2),
    semester INT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_grade (student_id, course_id, semester)
);

CREATE TABLE IF NOT EXISTS books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(20) UNIQUE,
    quantity INT DEFAULT 1,
    available INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS book_issues (
    id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    student_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    late_fee DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS hostel_rooms (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    capacity INT DEFAULT 2,
    occupied INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS hostel_allocations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    student_id INT NOT NULL,
    allocation_date DATE,
    FOREIGN KEY (room_id) REFERENCES hostel_rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY unique_allocation (student_id)
);

CREATE TABLE IF NOT EXISTS gate_passes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    out_date DATE,
    out_time TIME,
    in_date DATE,
    in_time TIME,
    reason TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    due_date DATE,
    payment_date DATE,
    status ENUM('PENDING', 'PARTIAL', 'PAID') DEFAULT 'PENDING',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS timetable (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    faculty_id INT NOT NULL,
    day ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(20),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, role) VALUES 
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- Insert sample data
INSERT INTO courses (name, code, credits, department, semester) VALUES
('Data Structures', 'CS101', 4, 'Computer Science', 3),
('Database Management', 'CS102', 4, 'Computer Science', 4),
('Web Development', 'CS103', 3, 'Computer Science', 5)
ON DUPLICATE KEY UPDATE code=code;

INSERT INTO hostel_rooms (room_number, capacity, occupied) VALUES
('A101', 2, 0),
('A102', 2, 0),
('A103', 3, 0),
('B201', 2, 0),
('B202', 2, 0)
ON DUPLICATE KEY UPDATE room_number=room_number;

INSERT INTO books (title, author, isbn, quantity, available) VALUES
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 5, 5),
('Clean Code', 'Robert C. Martin', '9780132350884', 3, 3),
('Design Patterns', 'Gang of Four', '9780201633610', 4, 4)
ON DUPLICATE KEY UPDATE isbn=isbn;

-- Create application user with password 'college123'
CREATE USER IF NOT EXISTS 'collegeapp'@'localhost' IDENTIFIED BY 'college123';
GRANT ALL PRIVILEGES ON college_management.* TO 'collegeapp'@'localhost';
FLUSH PRIVILEGES;

-- Show success message
SELECT 'Database setup complete!' AS Status;
SELECT 'User: collegeapp' AS Info;
SELECT 'Password: college123' AS Info;

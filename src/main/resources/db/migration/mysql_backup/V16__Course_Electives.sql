-- Add columns to courses table
ALTER TABLE courses ADD COLUMN course_type VARCHAR(20) DEFAULT 'CORE'; -- 'CORE', 'ELECTIVE'
ALTER TABLE courses ADD COLUMN capacity INT DEFAULT 60;
ALTER TABLE courses ADD COLUMN enrolled_count INT DEFAULT 0;

-- Create course_registrations table
CREATE TABLE course_registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    registration_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'REGISTERED', -- 'REGISTERED', 'DROPPED'
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE KEY unique_registration (student_id, course_id)
);

-- Index for performance
CREATE INDEX idx_registration_student ON course_registrations(student_id);
CREATE INDEX idx_registration_course ON course_registrations(course_id);

-- ===========================================
-- GRADE/MARKS MANAGEMENT TABLES
-- ===========================================

-- Add grades table
CREATE TABLE IF NOT EXISTS grades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    exam_type ENUM('MID_TERM', 'END_TERM', 'ASSIGNMENT', 'QUIZ', 'PROJECT') NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL,
    grade_letter VARCHAR(5),
    percentage DECIMAL(5,2),
    remarks VARCHAR(255),
    exam_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_grade (student_id, course_id, exam_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes for better performance
CREATE INDEX idx_grades_student ON grades(student_id);
CREATE INDEX idx_grades_course ON grades(course_id);
CREATE INDEX idx_grades_exam_type ON grades(exam_type);

-- Sample grades data (optional)
-- Uncomment to insert sample data after creating students and courses
/*
INSERT INTO grades (student_id, course_id, exam_type, marks_obtained, max_marks, grade_letter, percentage, exam_date) VALUES
(1, 1, 'MID_TERM', 85, 100, 'A', 85.00, '2024-03-15'),
(1, 1, 'END_TERM', 90, 100, 'A+', 90.00, '2024-06-15'),
(1, 2, 'MID_TERM', 75, 100, 'B+', 75.00, '2024-03-20'),
(1, 2, 'ASSIGNMENT', 45, 50, 'A+', 90.00, '2024-04-10');
*/

-- Phase 5: Advanced Features Schema

-- Club Announcements
CREATE TABLE club_announcements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    club_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    posted_by INT NOT NULL, -- Student President or Faculty Coordinator (User ID)
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE,
    FOREIGN KEY (posted_by) REFERENCES users(id),
    INDEX idx_club_announcement (club_id)
);

-- Event Collaboration (Multi-Department)
CREATE TABLE event_collaborators (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    department_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'ACCEPTED', 'DECLINED'
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_collab (event_id, department_id)
);

-- Event Resources (Physical Items)
CREATE TABLE event_resources (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    resource_name VARCHAR(100) NOT NULL, -- e.g., 'Projector', 'Microphone', 'Lab 1'
    quantity INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'REQUESTED', -- 'REQUESTED', 'APPROVED', 'DENIED'
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Event Volunteers
CREATE TABLE event_volunteers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    student_id INT NOT NULL,
    task_description VARCHAR(200), -- 'Cleanup', 'Registration Desk', 'Technical Support'
    status VARCHAR(20) DEFAULT 'REGISTERED', -- 'REGISTERED', 'APPROVED', 'COMPLETED'
    hours_logged FLOAT DEFAULT 0.0,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY unique_volunteer (event_id, student_id)
);

-- Student Feedback (Private from Faculty)
CREATE TABLE student_feedback (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    faculty_id INT NOT NULL, -- User ID of faculty
    feedback_text TEXT NOT NULL,
    category VARCHAR(50), -- 'ACADEMIC', 'BEHAVIOR', 'APPRECIATION'
    is_private BOOLEAN DEFAULT TRUE, -- If TRUE, only visible to Faculty and Admin, not the student (OR visible to student but private to others? User req: "module for faculty to store observations". Let's assume private to faculty by default, maybe visible to student if shared.)
    -- Actually, user said "store observations", implying private notes. But also "Student Feedback Tracking".
    -- Let's stick to "Observation" model.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES users(id),
    INDEX idx_student_feedback (student_id)
);

CREATE TABLE calendar_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    event_type VARCHAR(20) NOT NULL, -- HOLIDAY, EXAM, EVENT, DEADLINE
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial Data
INSERT INTO calendar_events (title, event_date, event_type, description) VALUES 
('Semester Start', CURRENT_DATE + INTERVAL 7 DAY, 'EVENT', 'Start of the new academic semester'),
('Mid-Sem Exams', CURRENT_DATE + INTERVAL 60 DAY, 'EXAM', 'Mid-term examinations for all courses'),
('Winter Break', CURRENT_DATE + INTERVAL 120 DAY, 'HOLIDAY', 'Winter vacation starts');

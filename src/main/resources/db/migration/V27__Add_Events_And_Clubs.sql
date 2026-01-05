-- Event and Activity Management Schema
-- Supports college events, fests, cultural activities, and student clubs

-- Events Table
CREATE TABLE events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL, -- 'FEST', 'CULTURAL', 'SPORTS', 'ACADEMIC', 'CLUB', 'SEMINAR'
    location VARCHAR(200),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_participants INT,
    registration_deadline DATETIME,
    created_by INT NOT NULL,
    status VARCHAR(20) DEFAULT 'UPCOMING', -- 'UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'
    banner_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_event_type (event_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

-- Event Registrations
CREATE TABLE event_registrations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    student_id INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attendance_status VARCHAR(20) DEFAULT 'REGISTERED', -- 'REGISTERED', 'ATTENDED', 'ABSENT'
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY unique_registration (event_id, student_id),
    INDEX idx_student (student_id),
    INDEX idx_event (event_id)
);

-- Student Clubs
CREATE TABLE clubs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- 'TECHNICAL', 'CULTURAL', 'SPORTS', 'SOCIAL', 'ACADEMIC'
    logo_path VARCHAR(500),
    president_student_id INT,
    faculty_coordinator_id INT,
    member_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'INACTIVE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (president_student_id) REFERENCES students(id) ON DELETE SET NULL,
    FOREIGN KEY (faculty_coordinator_id) REFERENCES faculty(id) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_status (status)
);

-- Club Memberships
CREATE TABLE club_memberships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    club_id INT NOT NULL,
    student_id INT NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER', -- 'PRESIDENT', 'VICE_PRESIDENT', 'SECRETARY', 'TREASURER', 'MEMBER'
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY unique_membership (club_id, student_id),
    INDEX idx_student (student_id),
    INDEX idx_club (club_id)
);

-- Club Activities (Links clubs to events they organize)
CREATE TABLE club_activities (
    id INT PRIMARY KEY AUTO_INCREMENT,
    club_id INT NOT NULL,
    event_id INT NOT NULL,
    FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY unique_club_event (club_id, event_id)
);

-- Add Event Management Permissions
INSERT INTO permissions (code, name, category, description)
SELECT 'MANAGE_EVENTS', 'Manage Events', 'Student Management', 'Create and manage college events, fests, and activities'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'MANAGE_EVENTS');

INSERT INTO permissions (code, name, category, description)
SELECT 'VIEW_EVENTS', 'View Events', 'Student Management', 'View and register for college events'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_EVENTS');

INSERT INTO permissions (code, name, category, description)
SELECT 'MANAGE_CLUBS', 'Manage Clubs', 'Student Management', 'Create and manage student clubs and organizations'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'MANAGE_CLUBS');

INSERT INTO permissions (code, name, category, description)
SELECT 'JOIN_CLUBS', 'Join Clubs', 'Student Management', 'Join and participate in student clubs'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'JOIN_CLUBS');

-- Assign permissions to roles
SET @manage_events_id = (SELECT id FROM permissions WHERE code = 'MANAGE_EVENTS' LIMIT 1);
SET @view_events_id = (SELECT id FROM permissions WHERE code = 'VIEW_EVENTS' LIMIT 1);
SET @manage_clubs_id = (SELECT id FROM permissions WHERE code = 'MANAGE_CLUBS' LIMIT 1);
SET @join_clubs_id = (SELECT id FROM permissions WHERE code = 'JOIN_CLUBS' LIMIT 1);

SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);
SET @faculty_role_id = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @student_role_id = (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1);

-- Admin gets all event and club permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @manage_events_id WHERE @admin_role_id IS NOT NULL AND @manage_events_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @view_events_id WHERE @admin_role_id IS NOT NULL AND @view_events_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @manage_clubs_id WHERE @admin_role_id IS NOT NULL AND @manage_clubs_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @join_clubs_id WHERE @admin_role_id IS NOT NULL AND @join_clubs_id IS NOT NULL;

-- Faculty gets event and club management
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @manage_events_id WHERE @faculty_role_id IS NOT NULL AND @manage_events_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @view_events_id WHERE @faculty_role_id IS NOT NULL AND @view_events_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @manage_clubs_id WHERE @faculty_role_id IS NOT NULL AND @manage_clubs_id IS NOT NULL;

-- Students get view and join permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @student_role_id, @view_events_id WHERE @student_role_id IS NOT NULL AND @view_events_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @student_role_id, @join_clubs_id WHERE @student_role_id IS NOT NULL AND @join_clubs_id IS NOT NULL;

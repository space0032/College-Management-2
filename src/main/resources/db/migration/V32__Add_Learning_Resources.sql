-- Syllabus and Learning Resources Schema

-- Resource Categories (e.g., Lecture Notes, Assignments, Lab Manuals, Past Papers)
CREATE TABLE resource_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pre-populate categories
INSERT INTO resource_categories (name, description) VALUES 
('Lecture Notes', 'Class notes and slides'),
('Lab Manual', 'Laboratory manuals and procedures'),
('Assignment', 'Assignment questions and guides'),
('Past Paper', 'Previous year question papers'),
('Reference Material', 'Additional reading and reference books'),
('Tutorial', 'Tutorial sheets and problems');

-- Course Syllabi
CREATE TABLE course_syllabi (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    version VARCHAR(50) DEFAULT '1.0',
    description TEXT,
    uploaded_by INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_course (course_id)
);

-- Learning Resources
CREATE TABLE learning_resources (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_id INT, -- Nullable for general resources, but typically linked to course
    category_id INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50), -- pdf, docx, pptx, etc.
    file_size BIGINT, -- in bytes
    uploaded_by INT NOT NULL,
    download_count INT DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE, -- If true, accessible to all, else restricted to course students
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES resource_categories(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_course_resource (course_id),
    INDEX idx_category (category_id)
);

-- Resource Tags (for searching)
CREATE TABLE resource_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    resource_id INT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES learning_resources(id) ON DELETE CASCADE,
    INDEX idx_tag (tag_name)
);

-- Permissions
INSERT INTO permissions (code, name, category, description)
SELECT 'UPLOAD_SYLLABUS', 'Upload Syllabus', 'Academic', 'Upload and manage course syllabi'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'UPLOAD_SYLLABUS');

INSERT INTO permissions (code, name, category, description)
SELECT 'UPLOAD_RESOURCES', 'Upload Resources', 'Academic', 'Upload and manage learning resources'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'UPLOAD_RESOURCES');

INSERT INTO permissions (code, name, category, description)
SELECT 'VIEW_RESOURCES', 'View Resources', 'Academic', 'View and download learning resources'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_RESOURCES');

-- Assign Permissions
SET @upload_syllabus_id = (SELECT id FROM permissions WHERE code = 'UPLOAD_SYLLABUS' LIMIT 1);
SET @upload_resources_id = (SELECT id FROM permissions WHERE code = 'UPLOAD_RESOURCES' LIMIT 1);
SET @view_resources_id = (SELECT id FROM permissions WHERE code = 'VIEW_RESOURCES' LIMIT 1);

SET @faculty_role_id = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @student_role_id = (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1);
SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);

-- Faculty: Upload Syllabus, Upload Resources, View Resources
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @upload_syllabus_id WHERE @faculty_role_id IS NOT NULL AND @upload_syllabus_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @upload_resources_id WHERE @faculty_role_id IS NOT NULL AND @upload_resources_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @faculty_role_id, @view_resources_id WHERE @faculty_role_id IS NOT NULL AND @view_resources_id IS NOT NULL;

-- Student: View Resources
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @student_role_id, @view_resources_id WHERE @student_role_id IS NOT NULL AND @view_resources_id IS NOT NULL;

-- Admin: All
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @upload_syllabus_id WHERE @admin_role_id IS NOT NULL AND @upload_syllabus_id IS NOT NULL;
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @upload_resources_id WHERE @admin_role_id IS NOT NULL AND @upload_resources_id IS NOT NULL;
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @view_resources_id WHERE @admin_role_id IS NOT NULL AND @view_resources_id IS NOT NULL;

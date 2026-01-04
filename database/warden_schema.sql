-- Add WARDEN role to users table
ALTER TABLE users MODIFY COLUMN role ENUM('ADMIN', 'FACULTY', 'STUDENT', 'WARDEN') NOT NULL;

-- Create Wardens table
CREATE TABLE IF NOT EXISTS wardens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    hostel_id INT DEFAULT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    -- Note: Foreign key to hostels table should ideally be added if hostel_id is intended to link there
    -- but for now we keep it simple or loose as requested schema
);

-- Hostel Management System Schema

-- Hostels Table
CREATE TABLE IF NOT EXISTS hostels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('BOYS', 'GIRLS', 'COED') NOT NULL,
    warden_name VARCHAR(100),
    warden_contact VARCHAR(20),
    total_rooms INT DEFAULT 0,
    total_capacity INT DEFAULT 0,
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hostel_id INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    occupied_count INT DEFAULT 0,
    room_type ENUM('AC', 'NON_AC') DEFAULT 'NON_AC',
    status ENUM('AVAILABLE', 'FULL', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room (hostel_id, room_number),
    INDEX idx_hostel (hostel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Hostel Allocations Table
CREATE TABLE IF NOT EXISTS hostel_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NULL,
    status ENUM('ACTIVE', 'VACATED') DEFAULT 'ACTIVE',
    remarks VARCHAR(500),
    allocated_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_room (room_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

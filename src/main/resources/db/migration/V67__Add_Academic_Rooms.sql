-- V67 academic rooms inventory for Room Availability feature
-- Replaces derived DISTINCT(room_number) inventory with a master table.

CREATE TABLE IF NOT EXISTS academic_rooms (
    id SERIAL PRIMARY KEY,
    room_number VARCHAR(50) UNIQUE NOT NULL,
    building VARCHAR(100),
    capacity INT NOT NULL DEFAULT 40,
    type VARCHAR(30) NOT NULL DEFAULT 'CLASSROOM'
        CHECK (type IN ('CLASSROOM','LABORATORY','SEMINAR','AUDITORIUM','OFFICE')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','MAINTENANCE','INACTIVE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_academic_rooms_status ON academic_rooms(status);
CREATE INDEX IF NOT EXISTS idx_academic_rooms_type ON academic_rooms(type);

-- Backfill from existing timetable room strings
INSERT INTO academic_rooms (room_number, type)
SELECT DISTINCT TRIM(room_number),
    CASE WHEN TRIM(room_number) ILIKE 'Lab%' THEN 'LABORATORY' ELSE 'CLASSROOM' END
FROM timetable
WHERE room_number IS NOT NULL AND TRIM(room_number) <> ''
ON CONFLICT (room_number) DO NOTHING;

-- Ensure room permissions exist (VIEW for checking, MANAGE for CRUD)
INSERT INTO permissions (code, name, category)
VALUES
    ('VIEW_ROOM', 'View Rooms', 'Academic'),
    ('MANAGE_ROOM', 'Manage Rooms', 'Academic')
ON CONFLICT (code) DO NOTHING;

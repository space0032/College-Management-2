-- V68 BOOK_ROOM permission: book an available academic room slot.
-- Booking creates a timetable entry; availability stays consistent via overlap checks.

INSERT INTO permissions (name, code, description, category)
VALUES ('Book Rooms', 'BOOK_ROOM', 'Book an available academic room slot (creates a timetable entry)', 'Academic')
ON CONFLICT (code) DO NOTHING;

-- Grant to roles that already own timetable creation
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code IN ('ADMIN', 'FACULTY')
AND p.code = 'BOOK_ROOM'
ON CONFLICT (role_id, permission_id) DO NOTHING;

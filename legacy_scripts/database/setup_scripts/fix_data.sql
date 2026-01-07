-- Fix missing fees and hostel allocations
USE college_management;

-- 1. FIX MISSING FEES
-- Insert default fees for any student who doesn't have a fee record
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status)
SELECT id, 75000.00, 0.00, '2026-01-31', NULL, 'PENDING'
FROM students
WHERE id NOT IN (SELECT student_id FROM fees);

SELECT ROW_COUNT() as 'Fees Records Created';

-- 2. ADD HOSTEL METADATA (If missing)
INSERT IGNORE INTO hostels (name, type, total_rooms, total_capacity) 
VALUES ('Main Hostel', 'COED', 100, 200);

-- 3. ADD MORE HOSTEL ROOMS
-- We need enough rooms for ~100+ hostelites. Adding 100 rooms (H101-H200) capacity 2
DROP PROCEDURE IF EXISTS AddRooms;
DELIMITER //
CREATE PROCEDURE AddRooms()
BEGIN
  DECLARE i INT DEFAULT 101;
  WHILE i <= 200 DO
    INSERT IGNORE INTO hostel_rooms (room_number, capacity, occupied)
    VALUES (CONCAT('H', i), 2, 0);
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;
CALL AddRooms();
DROP PROCEDURE AddRooms;

-- 4. ALLOCATE HOSTELS
-- Assign rooms to unallocated hostelites
DROP PROCEDURE IF EXISTS AllocateHostels;
DELIMITER //
CREATE PROCEDURE AllocateHostels()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE s_id INT;
  DECLARE r_id INT;
  
  -- Cursor for unallocated hostelites
  DECLARE student_cursor CURSOR FOR 
    SELECT id FROM students WHERE is_hostelite = 1 AND id NOT IN (SELECT student_id FROM hostel_allocations);
    
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  
  OPEN student_cursor;
  
  read_loop: LOOP
    FETCH student_cursor INTO s_id;
    IF done THEN
      LEAVE read_loop;
    END IF;
    
    -- Find available room
    SELECT id INTO r_id FROM hostel_rooms WHERE occupied < capacity LIMIT 1;
    
    IF r_id IS NOT NULL THEN
      INSERT INTO hostel_allocations (student_id, room_id, check_in_date, status) 
      VALUES (s_id, r_id, CURDATE(), 'ACTIVE');
      
      UPDATE hostel_rooms SET occupied = occupied + 1 WHERE id = r_id;
      SET r_id = NULL; 
    END IF;
    
  END LOOP;
  
  CLOSE student_cursor;
END //
DELIMITER ;

CALL AllocateHostels();
DROP PROCEDURE AllocateHostels;

SELECT ROW_COUNT() as 'Hostel Allocations Updated';

-- 5. FINAL VERIFICATION
SELECT 'Fees Summary' as Metric, COUNT(*) as Value FROM fees
UNION ALL
SELECT 'Hostel Allocations', COUNT(*) FROM hostel_allocations
UNION ALL
SELECT 'Hostel Rooms', COUNT(*) FROM hostel_rooms;

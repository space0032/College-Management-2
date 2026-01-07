-- Fix Hostel Data (Rooms and Allocations)
USE college_management;

-- 1. ADD ROOMS to 'rooms' table linked to Hostel 1
-- We assume Hostel ID 1 exists (created in previous step)
-- Add 150 rooms (101-250) to ensure plenty of space
DROP PROCEDURE IF EXISTS AddMainRooms;
DELIMITER //
CREATE PROCEDURE AddMainRooms()
BEGIN
  DECLARE i INT DEFAULT 101;
  DECLARE room_num VARCHAR(20);
  
  WHILE i <= 250 DO 
    SET room_num = CONCAT('R', i);
    -- Insert if not exists
    IF NOT EXISTS (SELECT 1 FROM rooms WHERE room_number = room_num) THEN
      INSERT INTO rooms (hostel_id, room_number, floor, capacity, occupied_count, room_type, status)
      VALUES (1, room_num, FLOOR(i/100), 2, 0, 'NON_AC', 'AVAILABLE');
    END IF;
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;
CALL AddMainRooms();
DROP PROCEDURE AddMainRooms;

-- 2. ALLOCATE HOSTELS
-- Assign rooms from 'rooms' table to unallocated hostelites
DROP PROCEDURE IF EXISTS AllocateHostels2;
DELIMITER //
CREATE PROCEDURE AllocateHostels2()
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
    
    -- Find available room in 'rooms' table
    SET r_id = NULL;
    SELECT id INTO r_id FROM rooms WHERE occupied_count < capacity AND status = 'AVAILABLE' LIMIT 1;
    
    IF r_id IS NOT NULL THEN
      INSERT INTO hostel_allocations (student_id, room_id, check_in_date, status) 
      VALUES (s_id, r_id, CURDATE(), 'ACTIVE');
      
      UPDATE rooms SET occupied_count = occupied_count + 1 WHERE id = r_id;
      
      -- Update status if full
      UPDATE rooms SET status = 'FULL' WHERE id = r_id AND occupied_count >= capacity;
      
    ELSE
      -- No rooms available, stop allocation
      LEAVE read_loop;
    END IF;
    
  END LOOP;
  
  CLOSE student_cursor;
END //
DELIMITER ;

CALL AllocateHostels2();
DROP PROCEDURE AllocateHostels2;

-- 3. VERIFICATION
SELECT 'Total Rooms' as Item, COUNT(*) as Count FROM rooms
UNION ALL
SELECT 'Total Allocations', COUNT(*) FROM hostel_allocations;

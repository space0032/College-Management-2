-- Clean up duplicate grades and add correct unique constraint

USE college_management;

-- Delete duplicate entries, keeping only the most recent one
DELETE g1 FROM grades g1
INNER JOIN grades g2 
WHERE g1.student_id = g2.student_id 
  AND g1.course_id = g2.course_id 
  AND g1.exam_type = g2.exam_type 
  AND g1.id < g2.id;

-- Add the correct unique constraint
ALTER TABLE grades ADD UNIQUE KEY unique_grade (student_id, course_id, exam_type);

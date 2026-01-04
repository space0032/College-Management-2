-- Add detailed profile columns to students table
ALTER TABLE students
ADD COLUMN dob DATE,
ADD COLUMN gender VARCHAR(20),
ADD COLUMN blood_group VARCHAR(10),
ADD COLUMN category VARCHAR(50), 
ADD COLUMN nationality VARCHAR(50),
ADD COLUMN father_name VARCHAR(100),
ADD COLUMN mother_name VARCHAR(100),
ADD COLUMN guardian_contact VARCHAR(20),
ADD COLUMN previous_school VARCHAR(200),
ADD COLUMN tenth_percentage DECIMAL(5,2),
ADD COLUMN twelfth_percentage DECIMAL(5,2),
ADD COLUMN extracurricular_activities TEXT,
ADD COLUMN profile_photo_path VARCHAR(255);

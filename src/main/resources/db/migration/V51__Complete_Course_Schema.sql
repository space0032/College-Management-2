-- CourseDAO reads and writes this optional classification field.
ALTER TABLE courses ADD COLUMN IF NOT EXISTS specialization VARCHAR(100);

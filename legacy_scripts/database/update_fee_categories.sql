-- Update Fee Categories to proper types
UPDATE fee_categories SET category_name = 'Tuition Fees' WHERE category_name LIKE '%tuition%' OR category_name LIKE '%academic%' OR id = 1;
UPDATE fee_categories SET category_name = 'Hostel Fees' WHERE category_name LIKE '%hostel%' OR category_name LIKE '%accommodation%' OR id = 2;
UPDATE fee_categories SET category_name = 'Exam Fees' WHERE category_name LIKE '%exam%' OR category_name LIKE '%examination%' OR id = 3;
UPDATE fee_categories SET category_name = 'Library Fees' WHERE category_name LIKE '%library%' OR id = 4;
UPDATE fee_categories SET category_name = 'Sports Fees' WHERE category_name LIKE '%sports%' OR id = 5;
UPDATE fee_categories SET category_name = 'Lab Fees' WHERE category_name LIKE '%lab%' OR category_name LIKE '%laboratory%' OR id = 6;

-- If categories don't exist, insert them
INSERT IGNORE INTO fee_categories (category_name, description) VALUES
('Tuition Fees', 'Academic tuition fees'),
('Hostel Fees', 'Hostel accommodation fees'),
('Exam Fees', 'Examination fees'),
('Library Fees', 'Library membership and book fees'),
('Sports Fees', 'Sports and athletics fees'),
('Lab Fees', 'Laboratory and practical fees');

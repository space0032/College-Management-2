-- ============================================================================
-- POPULATE LIBRARY WITH REALISTIC BOOK COLLECTION
-- ============================================================================
USE college_management;

-- Clear existing sample books
DELETE FROM books WHERE id > 0;

-- Reset auto increment
ALTER TABLE books AUTO_INCREMENT = 1;

-- ============================================================================
-- ADD COMPREHENSIVE BOOK COLLECTION
-- Schema: id, title, author, isbn, quantity, available, available_copies, total_copies
-- ============================================================================

INSERT INTO books (title, author, isbn, quantity, available, available_copies, total_copies) VALUES
-- Computer Science & Programming
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 15, 15, 15, 15),
('Design Patterns', 'Erich Gamma', '978-0201633610', 10, 10, 10, 10),
('Clean Code', 'Robert C. Martin', '978-0132350884', 12, 12, 12, 12),
('The Pragmatic Programmer', 'Andrew Hunt', '978-0135957059', 8, 8, 8, 8),
('Head First Java', 'Kathy Sierra', '978-0596009205', 15, 15, 15, 15),
('Effective Java', 'Joshua Bloch', '978-0134685991', 10, 10, 10, 10),
('Java: The Complete Reference', 'Herbert Schildt', '978-1260440232', 12, 12, 12, 12),
('Data Structures and Algorithms in Java', 'Robert Lafore', '978-0672324536', 10, 10, 10, 10),
('Artificial Intelligence: A Modern Approach', 'Stuart Russell', '978-0136042594', 8, 8, 8, 8),
('Database System Concepts', 'Abraham Silberschatz', '978-0078022159', 12, 12, 12, 12),
('Computer Networks', 'Andrew S. Tanenbaum', '978-0132126953', 10, 10, 10, 10),
('Operating System Concepts', 'Abraham Silberschatz', '978-1118063330', 12, 12, 12, 12),
('Machine Learning', 'Tom M. Mitchell', '978-0070428072', 6, 6, 6, 6),
('Python Crash Course', 'Eric Matthes', '978-1593279288', 10, 10, 10, 10),
('C Programming Language', 'Brian W. Kernighan', '978-0131103627', 15, 15, 15, 15),
('Deep Learning', 'Ian Goodfellow', '978-0262035613', 6, 6, 6, 6),
('Web Development with Node and Express', 'Ethan Brown', '978-1492053514', 8, 8, 8, 8),

-- Electrical Engineering
('Principles of Electric Circuits', 'Thomas L. Floyd', '978-0133760033', 12, 12, 12, 12),
('Electric Machinery Fundamentals', 'Stephen J. Chapman', '978-0073529547', 10, 10, 10, 10),
('Power System Analysis', 'John J. Grainger', '978-0070612938', 8, 8, 8, 8),
('Control Systems Engineering', 'Norman S. Nise', '978-1118170519', 10, 10, 10, 10),
('Electronic Devices and Circuit Theory', 'Robert L. Boylestad', '978-0132622264', 12, 12, 12, 12),
('Digital Design', 'M. Morris Mano', '978-0132774208', 10, 10, 10, 10),
('Microelectronic Circuits', 'Adel S. Sedra', '978-0199339136', 8, 8, 8, 8),
('Signals and Systems', 'Alan V. Oppenheim', '978-0138147570', 10, 10, 10, 10),
('Power Electronics', 'Muhammad H. Rashid', '978-0133125900', 8, 8, 8, 8),
('Electromagnetic Fields and Waves', 'Vladimir Rojansky', '978-0486638348', 6, 6, 6, 6),

-- Mechanical Engineering
('Engineering Mechanics: Statics', 'R.C. Hibbeler', '978-0133918922', 15, 15, 15, 15),
('Engineering Mechanics: Dynamics', 'R.C. Hibbeler', '978-0133915389', 15, 15, 15, 15),
('Mechanics of Materials', 'Ferdinand P. Beer', '978-0073398235', 12, 12, 12, 12),
('Thermodynamics: An Engineering Approach', 'Yunus A. Cengel', '978-0073398174', 12, 12, 12, 12),
('Fluid Mechanics', 'Frank M. White', '978-0073398273', 10, 10, 10, 10),
('Machine Design', 'Robert L. Norton', '978-0133356717', 10, 10, 10, 10),
('Theory of Machines and Mechanisms', 'Joseph E. Shigley', '978-0195371239', 8, 8, 8, 8),
('Manufacturing Processes for Engineering', 'Serope Kalpakjian', '978-0134290553', 10, 10, 10, 10),
('Heat and Mass Transfer', 'Yunus A. Cengel', '978-0073398181', 10, 10, 10, 10),
('Machine Elements in Mechanical Design', 'Robert L. Mott', '978-0135077931', 8, 8, 8, 8),
('Automotive Engineering Fundamentals', 'Richard Stone', '978-0768011210', 6, 6, 6, 6),

-- Civil Engineering
('Structural Analysis', 'R.C. Hibbeler', '978-0134382593', 12, 12, 12, 12),
('Design of Concrete Structures', 'Arthur H. Nilson', '978-0073397948', 10, 10, 10, 10),
('Soil Mechanics and Foundation Engineering', 'K.R. Arora', '978-8121900843', 10, 10, 10, 10),
('Transportation Engineering', 'C.S. Papacostas', '978-0132719704', 8, 8, 8, 8),
('Environmental Engineering', 'Howard S. Peavy', '978-0070494947', 8, 8, 8, 8),
('Surveying and Levelling', 'N.N. Basak', '978-0070965584', 10, 10, 10, 10),
('Structural Steel Design', 'Jack C. McCormac', '978-0136079484', 8, 8, 8, 8),
('Hydraulic Engineering', 'John D. Fenton', '978-3319008516', 6, 6, 6, 6),
('Construction Planning and Management', 'P.S. Gahlot', '978-8122419238', 8, 8, 8, 8),
('Earthquake Engineering', 'Robert W. Clough', '978-0135632185', 6, 6, 6, 6),

-- Mathematics
('Advanced Engineering Mathematics', 'Erwin Kreyszig', '978-0470458365', 15, 15, 15, 15),
('Linear Algebra and Its Applications', 'Gilbert Strang', '978-0030105678', 12, 12, 12, 12),
('Calculus: Early Transcendentals', 'James Stewart', '978-1285741550', 15, 15, 15, 15),
('Discrete Mathematics', 'Kenneth H. Rosen', '978-0073383095', 10, 10, 10, 10),
('Probability and Statistics', 'Morris H. DeGroot', '978-0321500465', 10, 10, 10, 10),
('Numerical Methods for Engineers', 'Steven C. Chapra', '978-0073397924', 8, 8, 8, 8),

-- Physics
('Physics for Scientists and Engineers', 'Raymond A. Serway', '978-1133947271', 12, 12, 12, 12),
('University Physics', 'Hugh D. Young', '978-0321973610', 12, 12, 12, 12),
('Introduction to Electrodynamics', 'David J. Griffiths', '978-1108420419', 8, 8, 8, 8),
('Quantum Mechanics', 'David J. Griffiths', '978-1107179866', 6, 6, 6, 6),
('Modern Physics', 'Kenneth S. Krane', '978-1118061145', 8, 8, 8, 8),

-- Chemistry
('Chemistry: The Central Science', 'Theodore E. Brown', '978-0134414232', 12, 12, 12, 12),
('Organic Chemistry', 'Paula Yurkanis Bruice', '978-0134042282', 10, 10, 10, 10),
('Physical Chemistry', 'Peter Atkins', '978-0198769866', 8, 8, 8, 8),
('Inorganic Chemistry', 'Gary L. Miessler', '978-0321811059', 8, 8, 8, 8),

-- General Reference & Skills
('The Elements of Style', 'William Strunk Jr.', '978-0205309023', 10, 10, 10, 10),
('Technical Writing', 'John M. Lannon', '978-0134261782', 8, 8, 8, 8),
('Engineering Ethics', 'Charles B. Fleddermann', '978-0132774260', 6, 6, 6, 6),
('Innovation and Entrepreneurship', 'Peter F. Drucker', '978-0060851132', 8, 8, 8, 8),
('The Lean Startup', 'Eric Ries', '978-0307887894', 6, 6, 6, 6),
('Thinking, Fast and Slow', 'Daniel Kahneman', '978-0374533557', 5, 5, 5, 5),
('Design of Everyday Things', 'Don Norman', '978-0465050659', 5, 5, 5, 5);

SELECT CONCAT('Successfully added ', COUNT(*), ' books to the library') as Status
FROM books;

SELECT CONCAT('Total Copies: ', SUM(total_copies)) as Inventory
FROM books;


-- Clear existing sample books (keep the table structure)
DELETE FROM books WHERE id > 0;

-- ============================================================================
-- COMPUTER SCIENCE BOOKS
-- ============================================================================
INSERT INTO books (title, author, isbn, publisher, category, quantity, available) VALUES
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'MIT Press', 'Computer Science', 15, 15),
('Design Patterns', 'Erich Gamma', '978-0201633610', 'Addison-Wesley', 'Computer Science', 10, 10),
('Clean Code', 'Robert C. Martin', '978-0132350884', 'Prentice Hall', 'Computer Science', 12, 12),
('The Pragmatic Programmer', 'Andrew Hunt', '978-0135957059', 'Addison-Wesley', 'Computer Science', 8, 8),
('Head First Java', 'Kathy Sierra', '978-0596009205', 'O\'Reilly Media', 'Computer Science', 15, 15),
('Effective Java', 'Joshua Bloch', '978-0134685991', 'Addison-Wesley', 'Computer Science', 10, 10),
('Java: The Complete Reference', 'Herbert Schildt', '978-1260440232', 'McGraw-Hill', 'Computer Science', 12, 12),
('Data Structures and Algorithms in Java', 'Robert Lafore', '978-0672324536', 'Sams Publishing', 'Computer Science', 10, 10),
('Artificial Intelligence: A Modern Approach', 'Stuart Russell', '978-0136042594', 'Pearson', 'Computer Science', 8, 8),
('Database System Concepts', 'Abraham Silberschatz', '978-0078022159', 'McGraw-Hill', 'Computer Science', 12, 12),
('Computer Networks', 'Andrew S. Tanenbaum', '978-0132126953', 'Pearson', 'Computer Science', 10, 10),
('Operating System Concepts', 'Abraham Silberschatz', '978-1118063330', 'Wiley', 'Computer Science', 12, 12),
('Machine Learning', 'Tom M. Mitchell', '978-0070428072', 'McGraw-Hill', 'Computer Science', 6, 6),
('Python Crash Course', 'Eric Matthes', '978-1593279288', 'No Starch Press', 'Computer Science', 10, 10),
('C Programming Language', 'Brian W. Kernighan', '978-0131103627', 'Prentice Hall', 'Computer Science', 15, 15),

-- ============================================================================
-- ELECTRICAL ENGINEERING BOOKS
-- ============================================================================
('Principles of Electric Circuits', 'Thomas L. Floyd', '978-0133760033', 'Pearson', 'Electrical Engineering', 12, 12),
('Electric Machinery Fundamentals', 'Stephen J. Chapman', '978-0073529547', 'McGraw-Hill', 'Electrical Engineering', 10, 10),
('Power System Analysis', 'John J. Grainger', '978-0070612938', 'McGraw-Hill', 'Electrical Engineering', 8, 8),
('Control Systems Engineering', 'Norman S. Nise', '978-1118170519', 'Wiley', 'Electrical Engineering', 10, 10),
('Electronic Devices and Circuit Theory', 'Robert L. Boylestad', '978-0132622264', 'Pearson', 'Electrical Engineering', 12, 12),
('Digital Design', 'M. Morris Mano', '978-0132774208', 'Pearson', 'Electrical Engineering', 10, 10),
('Microelectronic Circuits', 'Adel S. Sedra', '978-0199339136', 'Oxford University Press', 'Electrical Engineering', 8, 8),
('Signals and Systems', 'Alan V. Oppenheim', '978-0138147570', 'Pearson', 'Electrical Engineering', 10, 10),
('Power Electronics', 'Muhammad H. Rashid', '978-0133125900', 'Pearson', 'Electrical Engineering', 8, 8),
('Electromagnetic Fields and Waves', 'Vladimir Rojansky', '978-0486638348', 'Dover Publications', 'Electrical Engineering', 6, 6),

-- ============================================================================
-- MECHANICAL ENGINEERING BOOKS  
-- ============================================================================
('Engineering Mechanics: Statics', 'R.C. Hibbeler', '978-0133918922', 'Pearson', 'Mechanical Engineering', 15, 15),
('Engineering Mechanics: Dynamics', 'R.C. Hibbeler', '978-0133915389', 'Pearson', 'Mechanical Engineering', 15, 15),
('Mechanics of Materials', 'Ferdinand P. Beer', '978-0073398235', 'McGraw-Hill', 'Mechanical Engineering', 12, 12),
('Thermodynamics: An Engineering Approach', 'Yunus A. Cengel', '978-0073398174', 'McGraw-Hill', 'Mechanical Engineering', 12, 12),
('Fluid Mechanics', 'Frank M. White', '978-0073398273', 'McGraw-Hill', 'Mechanical Engineering', 10, 10),
('Machine Design', 'Robert L. Norton', '978-0133356717', 'Pearson', 'Mechanical Engineering', 10, 10),
('Theory of Machines and Mechanisms', 'Joseph E. Shigley', '978-0195371239', 'Oxford University Press', 'Mechanical Engineering', 8, 8),
('Manufacturing Processes for Engineering Materials', 'Serope Kalpakjian', '978-0134290553', 'Pearson', 'Mechanical Engineering', 10, 10),
('Heat and Mass Transfer', 'Yunus A. Cengel', '978-0073398181', 'McGraw-Hill', 'Mechanical Engineering', 10, 10),
('Machine Elements in Mechanical Design', 'Robert L. Mott', '978-0135077931', 'Pearson', 'Mechanical Engineering', 8, 8),

-- ============================================================================
-- CIVIL ENGINEERING BOOKS
-- ============================================================================
('Structural Analysis', 'R.C. Hibbeler', '978-0134382593', 'Pearson', 'Civil Engineering', 12, 12),
('Design of Concrete Structures', 'Arthur H. Nilson', '978-0073397948', 'McGraw-Hill', 'Civil Engineering', 10, 10),
('Soil Mechanics and Foundation Engineering', 'K.R. Arora', '978-8121900843', 'Standard Publishers', 'Civil Engineering', 10, 10),
('Transportation Engineering', 'C.S. Papacostas', '978-0132719704', 'Pearson', 'Civil Engineering', 8, 8),
('Environmental Engineering', 'Howard S. Peavy', '978-0070494947', 'McGraw-Hill', 'Civil Engineering', 8, 8),
('Surveying and Levelling', 'N.N. Basak', '978-0070965584', 'McGraw-Hill', 'Civil Engineering', 10, 10),
('Structural Steel Design', 'Jack C. McCormac', '978-0136079484', 'Pearson', 'Civil Engineering', 8, 8),
('Hydraulic Engineering', 'John D. Fenton', '978-3319008516', 'Springer', 'Civil Engineering', 6, 6),
('Construction Planning and Management', 'P.S. Gahlot', '978-8122419238', 'New Age International', 'Civil Engineering', 8, 8),
('Earthquake Engineering', 'Robert W. Clough', '978-0135632185', 'Pearson', 'Civil Engineering', 6, 6),

-- ============================================================================
-- MATHEMATICS BOOKS
-- ============================================================================
('Advanced Engineering Mathematics', 'Erwin Kreyszig', '978-0470458365', 'Wiley', 'Mathematics', 15, 15),
('Linear Algebra and Its Applications', 'Gilbert Strang', '978-0030105678', 'Brooks Cole', 'Mathematics', 12, 12),
('Calculus: Early Transcendentals', 'James Stewart', '978-1285741550', 'Cengage Learning', 'Mathematics', 15, 15),
('Discrete Mathematics and Its Applications', 'Kenneth H. Rosen', '978-0073383095', 'McGraw-Hill', 'Mathematics', 10, 10),
('Probability and Statistics', 'Morris H. DeGroot', '978-0321500465', 'Pearson', 'Mathematics', 10, 10),
('Numerical Methods for Engineers', 'Steven C. Chapra', '978-0073397924', 'McGraw-Hill', 'Mathematics', 8, 8),

-- ============================================================================
-- PHYSICS BOOKS
-- ============================================================================
('Physics for Scientists and Engineers', 'Raymond A. Serway', '978-1133947271', 'Cengage Learning', 'Physics', 12, 12),
('University Physics', 'Hugh D. Young', '978-0321973610', 'Pearson', 'Physics', 12, 12),
('Introduction to Electrodynamics', 'David J. Griffiths', '978-1108420419', 'Cambridge University Press', 'Physics', 8, 8),
('Quantum Mechanics', 'David J. Griffiths', '978-1107179866', 'Cambridge University Press', 'Physics', 6, 6),
('Modern Physics', 'Kenneth S. Krane', '978-1118061145', 'Wiley', 'Physics', 8, 8),

-- ============================================================================
-- CHEMISTRY BOOKS
-- ============================================================================
('Chemistry: The Central Science', 'Theodore E. Brown', '978-0134414232', 'Pearson', 'Chemistry', 12, 12),
('Organic Chemistry', 'Paula Yurkanis Bruice', '978-0134042282', 'Pearson', 'Chemistry', 10, 10),
('Physical Chemistry', 'Peter Atkins', '978-0198769866', 'Oxford University Press', 'Chemistry', 8, 8),
('Inorganic Chemistry', 'Gary L. Miessler', '978-0321811059', 'Pearson', 'Chemistry', 8, 8),

-- ============================================================================
-- GENERAL REFERENCE BOOKS
-- ============================================================================
('The Elements of Style', 'William Strunk Jr.', '978-0205309023', 'Pearson', 'Language', 10, 10),
('Technical Writing', 'John M. Lannon', '978-0134261782', 'Pearson', 'Language', 8, 8),
('Engineering Ethics', 'Charles B. Fleddermann', '978-0132774260', 'Pearson', 'Ethics', 6, 6),
('Innovation and Entrepreneurship', 'Peter F. Drucker', '978-0060851132', 'Harper Business', 'Management', 8, 8),
('The Lean Startup', 'Eric Ries', '978-0307887894', 'Crown Business', 'Management', 6, 6);

SELECT 'Library populated successfully!' as Status;
SELECT category, COUNT(*) as book_count, SUM(quantity) as total_copies
FROM books 
GROUP BY category 
ORDER BY category;

SELECT CONCAT('Total Books: ', COUNT(*), ' titles') as Summary,
       CONCAT('Total Copies: ', SUM(quantity)) as Inventory
FROM books;

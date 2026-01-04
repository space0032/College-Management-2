-- ============================================================================
-- ENHANCE LIBRARY AND POPULATE WITH COMPLETE DATA
-- ============================================================================
USE college_management;

-- Add missing columns to books table
ALTER TABLE books 
ADD COLUMN IF NOT EXISTS publisher VARCHAR(100) AFTER isbn,
ADD COLUMN IF NOT EXISTS category VARCHAR(50) AFTER publisher;

-- Clear existing books
DELETE FROM books WHERE id > 0;
ALTER TABLE books AUTO_INCREMENT = 1;

-- ============================================================================
-- POPULATE LIBRARY WITH COMPREHENSIVE BOOK COLLECTION
-- ============================================================================

INSERT INTO books (title, author, isbn, publisher, category, quantity, available, available_copies, total_copies) VALUES
-- Computer Science & Programming (17 books)
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'MIT Press', 'Computer Science', 15, 15, 15, 15),
('Design Patterns', 'Erich Gamma', '978-0201633610', 'Addison-Wesley', 'Computer Science', 10, 10, 10, 10),
('Clean Code', 'Robert C. Martin', '978-0132350884', 'Prentice Hall', 'Computer Science', 12, 12, 12, 12),
('The Pragmatic Programmer', 'Andrew Hunt', '978-0135957059', 'Addison-Wesley', 'Computer Science', 8, 8, 8, 8),
('Head First Java', 'Kathy Sierra', '978-0596009205', 'O''Reilly Media', 'Computer Science', 15, 15, 15, 15),
('Effective Java', 'Joshua Bloch', '978-0134685991', 'Addison-Wesley', 'Computer Science', 10, 10, 10, 10),
('Java: The Complete Reference', 'Herbert Schildt', '978-1260440232', 'McGraw-Hill', 'Computer Science', 12, 12, 12, 12),
('Data Structures and Algorithms in Java', 'Robert Lafore', '978-0672324536', 'Sams Publishing', 'Computer Science', 10, 10, 10, 10),
('Artificial Intelligence: A Modern Approach', 'Stuart Russell', '978-0136042594', 'Pearson', 'Computer Science', 8, 8, 8, 8),
('Database System Concepts', 'Abraham Silberschatz', '978-0078022159', 'McGraw-Hill', 'Computer Science', 12, 12, 12, 12),
('Computer Networks', 'Andrew S. Tanenbaum', '978-0132126953', 'Pearson', 'Computer Science', 10, 10, 10, 10),
('Operating System Concepts', 'Abraham Silberschatz', '978-1118063330', 'Wiley', 'Computer Science', 12, 12, 12, 12),
('Machine Learning', 'Tom M. Mitchell', '978-0070428072', 'McGraw-Hill', 'Computer Science', 6, 6, 6, 6),
('Python Crash Course', 'Eric Matthes', '978-1593279288', 'No Starch Press', 'Computer Science', 10, 10, 10, 10),
('C Programming Language', 'Brian W. Kernighan', '978-0131103627', 'Prentice Hall', 'Computer Science', 15, 15, 15, 15),
('Deep Learning', 'Ian Goodfellow', '978-0262035613', 'MIT Press', 'Computer Science', 6, 6, 6, 6),
('Web Development with Node', 'Ethan Brown', '978-1492053514', 'O''Reilly Media', 'Computer Science', 8, 8, 8, 8),

-- Electrical Engineering (10 books)
('Principles of Electric Circuits', 'Thomas L. Floyd', '978-0133760033', 'Pearson', 'Electrical Eng', 12, 12, 12, 12),
('Electric Machinery Fundamentals', 'Stephen J. Chapman', '978-0073529547', 'McGraw-Hill', 'Electrical Eng', 10, 10, 10, 10),
('Power System Analysis', 'John J. Grainger', '978-0070612938', 'McGraw-Hill', 'Electrical Eng', 8, 8, 8, 8),
('Control Systems Engineering', 'Norman S. Nise', '978-1118170519', 'Wiley', 'Electrical Eng', 10, 10, 10, 10),
('Electronic Devices and Circuit Theory', 'Robert L. Boylestad', '978-0132622264', 'Pearson', 'Electrical Eng', 12, 12, 12, 12),
('Digital Design', 'M. Morris Mano', '978-0132774208', 'Pearson', 'Electrical Eng', 10, 10, 10, 10),
('Microelectronic Circuits', 'Adel S. Sedra', '978-0199339136', 'Oxford University', 'Electrical Eng', 8, 8, 8, 8),
('Signals and Systems', 'Alan V. Oppenheim', '978-0138147570', 'Pearson', 'Electrical Eng', 10, 10, 10, 10),
('Power Electronics', 'Muhammad H. Rashid', '978-0133125900', 'Pearson', 'Electrical Eng', 8, 8, 8, 8),
('Electromagnetic Fields', 'Vladimir Rojansky', '978-0486638348', 'Dover Publications', 'Electrical Eng', 6, 6, 6, 6),

-- Mechanical Engineering (11 books)
('Engineering Mechanics: Statics', 'R.C. Hibbeler', '978-0133918922', 'Pearson', 'Mechanical Eng', 15, 15, 15, 15),
('Engineering Mechanics: Dynamics', 'R.C. Hibbeler', '978-0133915389', 'Pearson', 'Mechanical Eng', 15, 15, 15, 15),
('Mechanics of Materials', 'Ferdinand P. Beer', '978-0073398235', 'McGraw-Hill', 'Mechanical Eng', 12, 12, 12, 12),
('Thermodynamics', 'Yunus A. Cengel', '978-0073398174', 'McGraw-Hill', 'Mechanical Eng', 12, 12, 12, 12),
('Fluid Mechanics', 'Frank M. White', '978-0073398273', 'McGraw-Hill', 'Mechanical Eng', 10, 10, 10, 10),
('Machine Design', 'Robert L. Norton', '978-0133356717', 'Pearson', 'Mechanical Eng', 10, 10, 10, 10),
('Theory of Machines', 'Joseph E. Shigley', '978-0195371239', 'Oxford University', 'Mechanical Eng', 8, 8, 8, 8),
('Manufacturing Processes', 'Serope Kalpakjian', '978-0134290553', 'Pearson', 'Mechanical Eng', 10, 10, 10, 10),
('Heat and Mass Transfer', 'Yunus A. Cengel', '978-0073398181', 'McGraw-Hill', 'Mechanical Eng', 10, 10, 10, 10),
('Machine Elements', 'Robert L. Mott', '978-0135077931', 'Pearson', 'Mechanical Eng', 8, 8, 8, 8),
('Automotive Engineering', 'Richard Stone', '978-0768011210', 'SAE International', 'Mechanical Eng', 6, 6, 6, 6),

-- Civil Engineering (10 books)
('Structural Analysis', 'R.C. Hibbeler', '978-0134382593', 'Pearson', 'Civil Eng', 12, 12, 12, 12),
('Design of Concrete Structures', 'Arthur H. Nilson', '978-0073397948', 'McGraw-Hill', 'Civil Eng', 10, 10, 10, 10),
('Soil Mechanics', 'K.R. Arora', '978-8121900843', 'Standard Publishers', 'Civil Eng', 10, 10, 10, 10),
('Transportation Engineering', 'C.S. Papacostas', '978-0132719704', 'Pearson', 'Civil Eng', 8, 8, 8, 8),
('Environmental Engineering', 'Howard S. Peavy', '978-0070494947', 'McGraw-Hill', 'Civil Eng', 8, 8, 8, 8),
('Surveying and Levelling', 'N.N. Basak', '978-0070965584', 'McGraw-Hill', 'Civil Eng', 10, 10, 10, 10),
('Structural Steel Design', 'Jack C. McCormac', '978-0136079484', 'Pearson', 'Civil Eng', 8, 8, 8, 8),
('Hydraulic Engineering', 'John D. Fenton', '978-3319008516', 'Springer', 'Civil Eng', 6, 6, 6, 6),
('Construction Management', 'P.S. Gahlot', '978-8122419238', 'New Age Int.', 'Civil Eng', 8, 8, 8, 8),
('Earthquake Engineering', 'Robert W. Clough', '978-0135632185', 'Pearson', 'Civil Eng', 6, 6, 6, 6),

-- Mathematics (6 books)
('Advanced Engineering Mathematics', 'Erwin Kreyszig', '978-0470458365', 'Wiley', 'Mathematics', 15, 15, 15, 15),
('Linear Algebra', 'Gilbert Strang', '978-0030105678', 'Brooks Cole', 'Mathematics', 12, 12, 12, 12),
('Calculus', 'James Stewart', '978-1285741550', 'Cengage Learning', 'Mathematics', 15, 15, 15, 15),
('Discrete Mathematics', 'Kenneth H. Rosen', '978-0073383095', 'McGraw-Hill', 'Mathematics', 10, 10, 10, 10),
('Probability and Statistics', 'Morris H. DeGroot', '978-0321500465', 'Pearson', 'Mathematics', 10, 10, 10, 10),
('Numerical Methods', 'Steven C. Chapra', '978-0073397924', 'McGraw-Hill', 'Mathematics', 8, 8, 8, 8),

-- Physics (5 books)
('Physics for Engineers', 'Raymond A. Serway', '978-1133947271', 'Cengage Learning', 'Physics', 12, 12, 12, 12),
('University Physics', 'Hugh D. Young', '978-0321973610', 'Pearson', 'Physics', 12, 12, 12, 12),
('Electrodynamics', 'David J. Griffiths', '978-1108420419', 'Cambridge Univ.', 'Physics', 8, 8, 8, 8),
('Quantum Mechanics', 'David J. Griffiths', '978-1107179866', 'Cambridge Univ.', 'Physics', 6, 6, 6, 6),
('Modern Physics', 'Kenneth S. Krane', '978-1118061145', 'Wiley', 'Physics', 8, 8, 8, 8),

-- Chemistry (4 books)
('Chemistry: The Central Science', 'Theodore E. Brown', '978-0134414232', 'Pearson', 'Chemistry', 12, 12, 12, 12),
('Organic Chemistry', 'Paula Y. Bruice', '978-0134042282', 'Pearson', 'Chemistry', 10, 10, 10, 10),
('Physical Chemistry', 'Peter Atkins', '978-0198769866', 'Oxford University', 'Chemistry', 8, 8, 8, 8),
('Inorganic Chemistry', 'Gary L. Miessler', '978-0321811059', 'Pearson', 'Chemistry', 8, 8, 8, 8),

-- General Reference (7 books)
('The Elements of Style', 'William Strunk Jr.', '978-0205309023', 'Pearson', 'Language', 10, 10, 10, 10),
('Technical Writing', 'John M. Lannon', '978-0134261782', 'Pearson', 'Language', 8, 8, 8, 8),
('Engineering Ethics', 'Charles B. Fleddermann', '978-0132774260', 'Pearson', 'Ethics', 6, 6, 6, 6),
('Innovation', 'Peter F. Drucker', '978-0060851132', 'Harper Business', 'Management', 8, 8, 8, 8),
('The Lean Startup', 'Eric Ries', '978-0307887894', 'Crown Business', 'Management', 6, 6, 6, 6),
('Thinking Fast and Slow', 'Daniel Kahneman', '978-0374533557', 'Farrar Straus', 'Psychology', 5, 5, 5, 5),
('Design of Everyday Things', 'Don Norman', '978-0465050659', 'Basic Books', 'Design', 5, 5, 5, 5);

-- Verification
SELECT 'Library Enhancement Complete!' as Status;
SELECT category, COUNT(*) as Books, SUM(total_copies) as Copies
FROM books 
GROUP BY category 
ORDER BY category;
SELECT CONCAT('Total: ', COUNT(*), ' books, ', SUM(total_copies), ' copies') as Summary FROM books;

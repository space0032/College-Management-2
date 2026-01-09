-- Create Tables for Community Features (Crowdfunding & Scholarships)
CREATE TABLE IF NOT EXISTS campaigns (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    goal_amount DOUBLE PRECISION,
    raised_amount DOUBLE PRECISION,
    created_by INT,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS scholarships (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    amount DOUBLE PRECISION,
    donor_name VARCHAR(255),
    created_by INT,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS scholarship_applications (
    id SERIAL PRIMARY KEY,
    scholarship_id INT REFERENCES scholarships(id) ON DELETE CASCADE,
    student_id INT,
    statement TEXT,
    status VARCHAR(50)
);

-- Create Tables for Event Details (Budgets, Polls)
CREATE TABLE IF NOT EXISTS event_budgets (
    id SERIAL PRIMARY KEY,
    event_id INT REFERENCES events(id) ON DELETE CASCADE,
    item VARCHAR(255),
    estimated_cost DOUBLE PRECISION,
    actual_cost DOUBLE PRECISION,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS event_polls (
    id SERIAL PRIMARY KEY,
    event_id INT REFERENCES events(id) ON DELETE CASCADE,
    question VARCHAR(255),
    options TEXT,
    status VARCHAR(50)
);

-- Register New Permissions
INSERT INTO permissions (code, name, description, category) VALUES 
('CROWDFUNDING_VIEW', 'View Campaigns', 'View crowdfunding campaigns', 'COMMUNITY'),
('CROWDFUNDING_CREATE', 'Create Campaign', 'Create new crowdfunding campaigns', 'COMMUNITY'),
('CROWDFUNDING_DONATE', 'Donate to Campaign', 'Donate to crowdfunding campaigns', 'COMMUNITY'),
('SCHOLARSHIP_VIEW', 'View Scholarships', 'View scholarships', 'COMMUNITY'),
('SCHOLARSHIP_CREATE', 'Create Scholarship', 'Create new scholarships', 'COMMUNITY'),
('SCHOLARSHIP_APPLY', 'Apply for Scholarship', 'Apply for scholarships', 'COMMUNITY'),
('EVENT_BUDGET_VIEW', 'View Event Budget', 'View event budgets', 'ACADEMIC'),
('EVENT_BUDGET_EDIT', 'Edit Event Budget', 'Edit event budgets', 'ACADEMIC'),
('EVENT_POLL_VIEW', 'View Event Polls', 'View event polls', 'ACADEMIC'),
('EVENT_POLL_CREATE', 'Create Event Poll', 'Create event polls', 'ACADEMIC'),
('ROOM_CHECK', 'Check Room Availability', 'Check room availability status', 'ACADEMIC'),
('PAYROLL_MANAGE', 'Manage Payroll', 'Generate and manage payroll entries', 'FINANCE')
ON CONFLICT (code) DO NOTHING;

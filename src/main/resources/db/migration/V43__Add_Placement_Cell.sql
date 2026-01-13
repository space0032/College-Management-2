-- 1. Create Placement Companies Table
CREATE TABLE IF NOT EXISTS placement_companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    industry VARCHAR(100),
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create Placement Drives Table
CREATE TABLE IF NOT EXISTS placement_drives (
    id SERIAL PRIMARY KEY,
    company_id INT NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    package_lpa DOUBLE PRECISION NOT NULL,
    description TEXT,
    drive_date DATE NOT NULL,
    deadline DATE NOT NULL,
    eligibility_criteria TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES placement_companies(id) ON DELETE CASCADE
);

-- 3. Create Placement Applications Table
CREATE TABLE IF NOT EXISTS placement_applications (
    id SERIAL PRIMARY KEY,
    drive_id INT NOT NULL,
    student_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'APPLIED' CHECK (status IN ('APPLIED', 'SHORTLISTED', 'SELECTED', 'REJECTED')),
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drive_id) REFERENCES placement_drives(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (drive_id, student_id)
);

-- 4. Add Permission for Placment Management
INSERT INTO permissions (code, name, category, description) 
VALUES ('MANAGE_PLACEMENTS', 'Manage Placements', 'PLACEMENT', 'Allows managing companies, drives and applications')
ON CONFLICT (code) DO NOTHING;

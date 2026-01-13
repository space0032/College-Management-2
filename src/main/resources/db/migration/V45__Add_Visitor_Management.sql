-- 1. Create Visitors Table
CREATE TABLE IF NOT EXISTS visitors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(phone)
);

-- 2. Create Visitor Logs Table
CREATE TABLE IF NOT EXISTS visitor_logs (
    id SERIAL PRIMARY KEY,
    visitor_id INT NOT NULL,
    entry_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    purpose TEXT,
    person_to_meet VARCHAR(100),
    gate_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'IN' CHECK (status IN ('IN', 'OUT')),
    FOREIGN KEY (visitor_id) REFERENCES visitors(id) ON DELETE CASCADE
);

-- 3. Add Permission for Visitor Management (Gatekeeper & Admin)
INSERT INTO permissions (code, name, category, description) 
VALUES ('MANAGE_VISITORS', 'Manage Visitors', 'SECURITY', 'Allows logging visitor entry and exit')
ON CONFLICT (code) DO NOTHING;

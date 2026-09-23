-- V79: Student Affairs module (admin)
--
-- Backs the web "Student Affairs" page (Disciplinary Records, Grievance
-- System, Parent Communications). Previously the frontend called /api/affairs/*
-- endpoints that were never registered on the server, so every request fell
-- through to the root handler and the page crashed on render.

CREATE TABLE IF NOT EXISTS affairs_incidents (
    id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    incident_type VARCHAR(100) NOT NULL,
    action_taken TEXT,
    severity VARCHAR(20) DEFAULT 'Low',
    status VARCHAR(20) DEFAULT 'Under Review',
    incident_date DATE,
    reported_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_affairs_incidents_student ON affairs_incidents(student_id);
CREATE INDEX IF NOT EXISTS idx_affairs_incidents_status ON affairs_incidents(status);

CREATE TABLE IF NOT EXISTS affairs_grievances (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    category VARCHAR(100) DEFAULT 'Infrastructure',
    title VARCHAR(255) NOT NULL,
    description TEXT,
    reporter VARCHAR(100),
    priority VARCHAR(20) DEFAULT 'Medium',
    status VARCHAR(20) DEFAULT 'Open',
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_affairs_grievances_status ON affairs_grievances(status);

CREATE TABLE IF NOT EXISTS affairs_communications (
    id SERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) DEFAULT 'All Parents',
    channel VARCHAR(50) DEFAULT 'Email',
    message TEXT,
    sent_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_affairs_communications_sent_at ON affairs_communications(sent_at);
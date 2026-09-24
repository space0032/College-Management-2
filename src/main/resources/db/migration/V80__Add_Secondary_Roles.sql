-- Secondary Roles
-- A user keeps a single primary role in users.role_id (which drives display and
-- portal logic). This table stores additional roles whose PERMISSIONS are added
-- to the user's effective permission set ("perks"), without changing the role
-- shown to other users.

CREATE TABLE IF NOT EXISTS user_secondary_roles (
    user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);
INSERT INTO permissions (code, name, category, description) 
VALUES ('VIEW_PLACEMENTS', 'View Placement Cell', 'PLACEMENT', 'Allows viewing placement drives and applications')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- V55: Grant Event Detail Permissions
--
-- Adds the permission codes used by the new /api/event-details endpoints
-- (collaborators, event resources, volunteers) and grants them to the
-- appropriate roles. VIEW_VOLUNTEER and MANAGE_VOLUNTEER already exist from
-- V54; the remaining collaborator / event-resource / volunteer-action codes
-- are new.
--
-- All statements are idempotent (ON CONFLICT ... DO NOTHING).
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) Register the new permission codes.
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, name, category) VALUES
-- Event collaborators
('VIEW_COLLABORATOR', 'View Event Collaborators', 'Events'),
('CREATE_COLLABORATOR', 'Add Event Collaborators', 'Events'),
('DELETE_COLLABORATOR', 'Remove Event Collaborators', 'Events'),

-- Event resources
('VIEW_EVENT_RESOURCE', 'View Event Resources', 'Events'),
('CREATE_EVENT_RESOURCE', 'Add Event Resources', 'Events'),
('UPDATE_EVENT_RESOURCE', 'Update Event Resources', 'Events'),
('DELETE_EVENT_RESOURCE', 'Remove Event Resources', 'Events'),

-- Volunteers (action codes; VIEW_VOLUNTEER / MANAGE_VOLUNTEER already exist)
('REGISTER_VOLUNTEER', 'Register as Event Volunteer', 'Community'),
('UPDATE_VOLUNTEER', 'Update Volunteer Tasks', 'Community')

ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) Grant every permission to ADMIN (superuser role).
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3) Grant subsets to non-admin roles.
-- ---------------------------------------------------------------------------

-- FACULTY: manage collaborators, resources and volunteer tasks for events.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_COLLABORATOR', 'CREATE_COLLABORATOR', 'DELETE_COLLABORATOR',
    'VIEW_EVENT_RESOURCE', 'CREATE_EVENT_RESOURCE', 'UPDATE_EVENT_RESOURCE', 'DELETE_EVENT_RESOURCE',
    'VIEW_VOLUNTEER', 'UPDATE_VOLUNTEER'
)
WHERE r.code = 'FACULTY'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- STUDENT: view collaborators/resources and register as a volunteer.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_COLLABORATOR', 'VIEW_EVENT_RESOURCE',
    'VIEW_VOLUNTEER', 'REGISTER_VOLUNTEER'
)
WHERE r.code = 'STUDENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

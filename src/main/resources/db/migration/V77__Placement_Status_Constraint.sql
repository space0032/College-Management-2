-- V77 Placement Application Status Constraint
--
-- The web placement UI transitions applications through INTERVIEWING and
-- OFFERED, but the V43 CHECK only allowed APPLIED/SHORTLISTED/SELECTED/REJECTED,
-- so status updates from the web threw a constraint violation (23514).
-- Widen the CHECK to accept all statuses used by both the web and JavaFX UIs.
ALTER TABLE placement_applications DROP CONSTRAINT IF EXISTS placement_applications_status_check;
ALTER TABLE placement_applications ADD CONSTRAINT placement_applications_status_check
    CHECK (status IN ('APPLIED', 'SHORTLISTED', 'SELECTED', 'REJECTED', 'INTERVIEWING', 'OFFERED'));
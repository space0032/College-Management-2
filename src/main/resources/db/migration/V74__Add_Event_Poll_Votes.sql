-- ============================================================================
-- V74: Create event_poll_votes table
--
-- Problem: EventDAO.voteInPoll and getPollResults query a table named
-- `event_poll_votes` that no migration ever created, so every poll vote failed
-- with a "table not found" SQL error (500).
--
-- The unique constraint on (poll_id, student_id) makes the vote-by-student
-- upsert (EventDAO.voteInPoll) idempotent: voting twice for the same poll
-- updates the student's selected option instead of inserting a duplicate row.
-- ============================================================================

CREATE TABLE IF NOT EXISTS event_poll_votes (
    id SERIAL PRIMARY KEY,
    poll_id INT,
    student_id INT,
    selected_option VARCHAR(255),
    UNIQUE (poll_id, student_id),
    FOREIGN KEY (poll_id) REFERENCES event_polls(id) ON DELETE CASCADE
);
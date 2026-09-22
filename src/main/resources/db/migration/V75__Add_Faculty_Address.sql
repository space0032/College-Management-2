-- Faculty profile editing (ProfilePage contact tab) needs an address column
-- mirroring students.address; phone already exists on faculty.
ALTER TABLE faculty ADD COLUMN IF NOT EXISTS address TEXT;
-- V11: Fix Club Memberships Table
-- Add missing 'status' column which is used by ClubDAO

ALTER TABLE club_memberships ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';

-- V63: Per-track program fees (track inside department).
-- program_fee_structure gains specialization ('' = applies to all tracks in the department).
-- Track-specific rows override department defaults at enrollment time.

ALTER TABLE program_fee_structure ADD COLUMN IF NOT EXISTS specialization VARCHAR(100) DEFAULT '';
UPDATE program_fee_structure SET specialization = '' WHERE specialization IS NULL;

DROP INDEX IF EXISTS uq_program_fee_structure;
CREATE UNIQUE INDEX IF NOT EXISTS uq_program_fee_structure_spec
    ON program_fee_structure (department, specialization, category_id, academic_year);

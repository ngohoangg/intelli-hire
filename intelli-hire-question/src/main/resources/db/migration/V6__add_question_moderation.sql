ALTER TABLE questions
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN submitted_by VARCHAR(255),
    ADD COLUMN submitted_by_id BIGINT,
    ADD COLUMN rejection_reason TEXT;

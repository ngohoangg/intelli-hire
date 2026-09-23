-- Change user_id from BIGINT to VARCHAR(255) to match Interview Service
ALTER TABLE grading_sessions ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::VARCHAR;

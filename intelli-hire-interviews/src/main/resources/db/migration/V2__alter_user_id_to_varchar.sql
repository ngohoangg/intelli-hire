ALTER TABLE interview_sessions
    ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::VARCHAR;

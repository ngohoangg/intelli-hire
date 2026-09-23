ALTER TABLE question_logs
    ALTER COLUMN question_id TYPE VARCHAR(255) USING question_id::VARCHAR;

ALTER TABLE user_answers_raw
    ALTER COLUMN question_id TYPE VARCHAR(255) USING question_id::VARCHAR;

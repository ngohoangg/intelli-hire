-- =============================================
-- Add composite index on (topic_id, is_active)
-- for optimizing GET /topics/{id}/questions query
-- =============================================

CREATE INDEX IF NOT EXISTS idx_questions_topic_id_is_active ON questions(topic_id, is_active);

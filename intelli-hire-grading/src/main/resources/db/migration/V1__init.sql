-- Grading Sessions table
CREATE TABLE IF NOT EXISTS grading_sessions (
    id              VARCHAR(36) PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    topic           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    total_questions INTEGER DEFAULT 0,
    graded_count    INTEGER DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- Grading Results table
CREATE TABLE IF NOT EXISTS grading_results (
    id                 VARCHAR(36) PRIMARY KEY,
    grading_session_id VARCHAR(36) NOT NULL REFERENCES grading_sessions(id) ON DELETE CASCADE,
    question_id        VARCHAR(36) NOT NULL,
    question_text      TEXT,
    answer_text        TEXT,
    skill_name         VARCHAR(255),
    score              DOUBLE PRECISION DEFAULT 0,
    max_score          DOUBLE PRECISION DEFAULT 10,
    feedback           TEXT,
    prompt_used        TEXT,
    model_used         VARCHAR(100),
    latency_ms         BIGINT,
    created_at         TIMESTAMP DEFAULT NOW(),
    created_by         VARCHAR(255),
    last_updated_at    TIMESTAMP DEFAULT NOW(),
    last_updated_by    VARCHAR(255)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_grading_sessions_session_id ON grading_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_user_id ON grading_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_status ON grading_sessions(status);
CREATE INDEX IF NOT EXISTS idx_grading_results_session_id ON grading_results(grading_session_id);
CREATE INDEX IF NOT EXISTS idx_grading_results_question_id ON grading_results(question_id);

-- Interview Reports table
CREATE TABLE IF NOT EXISTS interview_reports (
    id              VARCHAR(36) PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    topic           VARCHAR(255),
    total_score     DOUBLE PRECISION DEFAULT 0,
    max_score       DOUBLE PRECISION DEFAULT 0,
    summary         TEXT,
    roadmap_json    JSONB,
    total_questions INTEGER DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- Skill Scores table (scores per skill in a report)
CREATE TABLE IF NOT EXISTS skill_scores (
    id              VARCHAR(36) PRIMARY KEY,
    report_id       VARCHAR(36) NOT NULL REFERENCES interview_reports(id) ON DELETE CASCADE,
    skill_name      VARCHAR(255) NOT NULL,
    score           DOUBLE PRECISION DEFAULT 0,
    max_score       DOUBLE PRECISION DEFAULT 0,
    question_count  INTEGER DEFAULT 0,
    feedback        TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- Question Scores table (score per question in a report)
CREATE TABLE IF NOT EXISTS question_scores (
    id              VARCHAR(36) PRIMARY KEY,
    report_id       VARCHAR(36) NOT NULL REFERENCES interview_reports(id) ON DELETE CASCADE,
    question_id     VARCHAR(36) NOT NULL,
    question_text   TEXT,
    answer_text     TEXT,
    score           DOUBLE PRECISION DEFAULT 0,
    max_score       DOUBLE PRECISION DEFAULT 10,
    feedback        TEXT,
    skill_name      VARCHAR(255),
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- User Progress table (track skill scores over time)
CREATE TABLE IF NOT EXISTS user_progress (
    id              VARCHAR(36) PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    skill_name      VARCHAR(255) NOT NULL,
    latest_score    DOUBLE PRECISION DEFAULT 0,
    average_score   DOUBLE PRECISION DEFAULT 0,
    total_attempts  INTEGER DEFAULT 0,
    score_trend     JSONB,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_reports_user_id ON interview_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_user_status ON interview_reports(user_id, status);
CREATE INDEX IF NOT EXISTS idx_skill_scores_report_id ON skill_scores(report_id);
CREATE INDEX IF NOT EXISTS idx_question_scores_report_id ON question_scores(report_id);
CREATE INDEX IF NOT EXISTS idx_question_scores_question_id ON question_scores(question_id);
CREATE INDEX IF NOT EXISTS idx_user_progress_user_id ON user_progress(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_progress_user_skill ON user_progress(user_id, skill_name);

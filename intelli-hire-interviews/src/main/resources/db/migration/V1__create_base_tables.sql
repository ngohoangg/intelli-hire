CREATE TABLE IF NOT EXISTS interview_sessions (
    id          BIGSERIAL     PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    topic       VARCHAR(100)  NOT NULL,
    difficulty  VARCHAR(20),
    status      VARCHAR(20)   NOT NULL DEFAULT 'CREATED',
    started_at  TIMESTAMP     DEFAULT NOW(),
    ended_at    TIMESTAMP,
    created_at  TIMESTAMP     DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON interview_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status  ON interview_sessions(status);

CREATE TABLE IF NOT EXISTS question_logs (
    id               BIGSERIAL PRIMARY KEY,
    session_id       BIGINT    NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question_id      BIGINT    NOT NULL,
    question_text    TEXT      NOT NULL,
    question_order   INT       NOT NULL,
    asked_at         TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qlogs_session_id ON question_logs(session_id);

CREATE TABLE IF NOT EXISTS user_answers_raw (
    id           BIGSERIAL PRIMARY KEY,
    session_id   BIGINT    NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question_id  BIGINT    NOT NULL,
    answer_text  TEXT      NOT NULL,
    submitted_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_answers_session_id ON user_answers_raw(session_id);

-- =============================================
-- Intelli Hire Question Service - Initial Schema
-- =============================================

-- Create topics table (self-referencing for hierarchy)
CREATE TABLE  IF NOT EXISTS topics (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id VARCHAR(36),
    "order" INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36),

    CONSTRAINT fk_topics_parent
        FOREIGN KEY (parent_id) REFERENCES topics(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX  IF NOT EXISTS idx_topics_parent_id ON topics(parent_id);
CREATE INDEX  IF NOT EXISTS idx_topics_is_active ON topics(is_active);
CREATE INDEX  IF NOT EXISTS idx_topics_order ON topics("order");

-- Create difficulty_levels table
CREATE TABLE  IF NOT EXISTS difficulty_levels (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    weight INT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36)
);

-- Unique constraint on name
CREATE UNIQUE INDEX  IF NOT EXISTS idx_difficulty_levels_name ON difficulty_levels(name);

-- Index on weight for sorting
CREATE INDEX  IF NOT EXISTS idx_difficulty_levels_weight ON difficulty_levels(weight);

-- Create tags table
CREATE TABLE  IF NOT EXISTS tags (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36)
);

-- Unique constraint on name
CREATE UNIQUE INDEX  IF NOT EXISTS idx_tags_name ON tags(name);

-- Create questions table
CREATE TABLE  IF NOT EXISTS questions (
    id VARCHAR(36) PRIMARY KEY,
    topic_id VARCHAR(36) NOT NULL,
    difficulty_id VARCHAR(36) NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36),

    -- Foreign keys
    CONSTRAINT fk_questions_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_questions_difficulty
        FOREIGN KEY (difficulty_id) REFERENCES difficulty_levels(id),

    -- Check constraint for question_type
    CONSTRAINT chk_question_type
        CHECK (question_type IN ('technical', 'behavioral', 'coding'))
);

-- Indexes
CREATE INDEX  IF NOT EXISTS idx_questions_topic_id ON questions(topic_id);
CREATE INDEX  IF NOT EXISTS idx_questions_difficulty_id ON questions(difficulty_id);
CREATE INDEX  IF NOT EXISTS idx_questions_is_active ON questions(is_active);
CREATE INDEX  IF NOT EXISTS idx_questions_type ON questions(question_type);

-- Create rubrics table (1-1 relationship with questions)
CREATE TABLE  IF NOT EXISTS rubrics (
    id VARCHAR(36) PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL,
    criteria TEXT,
    keywords JSON,
    example_answer TEXT,
    common_mistakes JSON,
    hints JSON,
    max_score INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36),

    -- Foreign key
    CONSTRAINT fk_rubrics_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Unique constraint for 1-1 relationship
CREATE UNIQUE INDEX  IF NOT EXISTS idx_rubrics_question_id ON rubrics(question_id);

-- Create question_tags table (many-to-many relationship)
CREATE TABLE  IF NOT EXISTS question_tags (
    question_id VARCHAR(36) NOT NULL,
    tag_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36),

    -- Composite primary key
    PRIMARY KEY (question_id, tag_id),

    -- Foreign keys
    CONSTRAINT fk_question_tags_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_question_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Index for reverse lookup (find questions by tag)
CREATE INDEX  IF NOT EXISTS idx_question_tags_tag_id ON question_tags(tag_id);

-- Create study_articles table
CREATE TABLE  IF NOT EXISTS study_articles (
    id VARCHAR(36) PRIMARY KEY,
    topic_id VARCHAR(36) NOT NULL,
    difficulty_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description TEXT,
    source VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(36),

    -- Foreign keys
    CONSTRAINT fk_study_articles_topic
        FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_study_articles_difficulty
        FOREIGN KEY (difficulty_id) REFERENCES difficulty_levels(id)
);

-- Indexes
CREATE INDEX  IF NOT EXISTS idx_study_articles_topic_id ON study_articles(topic_id);
CREATE INDEX  IF NOT EXISTS idx_study_articles_difficulty_id ON study_articles(difficulty_id);
CREATE INDEX  IF NOT EXISTS idx_study_articles_is_active ON study_articles(is_active);
CREATE INDEX  IF NOT EXISTS idx_study_articles_source ON study_articles(source);

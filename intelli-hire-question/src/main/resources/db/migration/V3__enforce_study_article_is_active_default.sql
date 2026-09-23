-- =============================================
-- Enforce study_articles.is_active default and nullability
-- =============================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = current_schema() AND table_name = 'study_articles'
    ) THEN
        UPDATE study_articles
        SET is_active = TRUE
        WHERE is_active IS NULL;

        ALTER TABLE study_articles
        ALTER COLUMN is_active SET DEFAULT TRUE;

        -- Only set NOT NULL if no NULLs remain
        IF NOT EXISTS (SELECT 1 FROM study_articles WHERE is_active IS NULL) THEN
            ALTER TABLE study_articles
            ALTER COLUMN is_active SET NOT NULL;
        END IF;
    END IF;
END $$;

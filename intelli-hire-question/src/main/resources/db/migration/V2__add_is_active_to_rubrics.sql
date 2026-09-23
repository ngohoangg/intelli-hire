-- =============================================
-- Add is_active column to rubrics table
-- =============================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema() AND table_name = 'rubrics' AND column_name = 'is_active'
    ) THEN
        ALTER TABLE rubrics ADD COLUMN is_active BOOLEAN DEFAULT TRUE NOT NULL;
    END IF;
END $$;

-- Index for filtering active rubrics
CREATE INDEX IF NOT EXISTS idx_rubrics_is_active ON rubrics(is_active);

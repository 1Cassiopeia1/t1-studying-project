ALTER TABLE IF EXISTS client
    ADD COLUMN IF NOT EXISTS status varchar(30);

BEGIN;

ALTER TABLE devices ADD COLUMN audio_out INTEGER NOT NULL DEFAULT 1;
ALTER TABLE devices ADD COLUMN work_start_at TIME WITHOUT TIME ZONE DEFAULT '00:00';
ALTER TABLE devices ADD COLUMN work_end_at TIME WITHOUT TIME ZONE DEFAULT '23:00';

ALTER TABLE devices ADD COLUMN mon BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN tue BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN wed BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN thu BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN fri BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN sat BOOLEAN  NOT NULL DEFAULT TRUE;
ALTER TABLE devices ADD COLUMN sun BOOLEAN  NOT NULL DEFAULT TRUE;

ALTER TABLE devices ADD COLUMN current_file_id INTEGER;
ALTER TABLE devices ADD COLUMN current_campaign_id INTEGER;
ALTER TABLE devices ADD COLUMN loading_campaign_id INTEGER;
ALTER TABLE devices ADD COLUMN loading_compaign_progress INTEGER;

ALTER TABLE devices ADD COLUMN cache BIGINT NOT NULL DEFAULT 0;
ALTER TABLE devices ADD COLUMN clear_cache BOOLEAN  NOT NULL DEFAULT FALSE;

ALTER TABLE devices ADD COLUMN is_on_top BOOLEAN  NOT NULL DEFAULT FALSE;
ALTER TABLE devices ADD COLUMN open_app BOOLEAN  NOT NULL DEFAULT FALSE;

ALTER TABLE devices ADD COLUMN next_file INTEGER;

ALTER TABLE devices ADD COLUMN created_dt TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

ALTER TABLE ad_campaigns ADD COLUMN count_files INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ad_campaigns ADD COLUMN count_images INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ad_campaigns ADD COLUMN count_audios INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ad_campaigns ADD COLUMN count_videos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ad_campaigns ADD COLUMN audio_length BIGINT NOT NULL DEFAULT 0;
ALTER TABLE ad_campaigns ADD COLUMN video_length BIGINT NOT NULL DEFAULT 0;

ALTER TABLE ad_campaigns ADD COLUMN files_archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ad_campaigns ADD COLUMN created_dt TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

ALTER TABLE files ADD COLUMN content_length BIGINT NOT NULL DEFAULT 0;
ALTER TABLE files ADD COLUMN angle INTEGER NOT NULL DEFAULT 0;

UPDATE campaigns_files SET order_id = file_id;

ALTER TABLE campaigns_files ADD COLUMN page INTEGER;
ALTER TABLE campaigns_files ADD COLUMN updated_dt TIMESTAMP WITHOUT TIME ZONE;

UPDATE campaigns_files SET updated_dt = created_dt;

COMMIT;
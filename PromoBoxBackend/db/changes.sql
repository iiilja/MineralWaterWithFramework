BEGIN;

CREATE TABLE error_log
(
  id bigserial NOT NULL,
  name character varying(255),
  message character varying(255),
  stack_trace text,
  date timestamp without time zone,
  CONSTRAINT pk_error_log_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);


ALTER TABLE devices ADD COLUMN video_wall BOOLEAN;
ALTER TABLE devices ADD COLUMN row_count INTEGER;
ALTER TABLE devices ADD COLUMN monitor_per_row INTEGER;
ALTER TABLE devices ADD COLUMN resolution_vertical INTEGER;
ALTER TABLE devices ADD COLUMN resolution_horizontal INTEGER;
ALTER TABLE devices ADD COLUMN frame_size INTEGER;


COMMIT;
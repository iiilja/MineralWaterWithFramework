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

ALTER TABLE devices ADD COLUMN video_wall boolean DEFAULT FALSE;
ALTER TABLE devices ADD COLUMN resolution_vertical integer;
ALTER TABLE devices ADD COLUMN resolution_horizontal integer;

CREATE TABLE devices_displays
(
	display_id INTEGER NOT NULL,
	device_id INTEGER NOT NULL,
	point1 character varying(30),
	point2 character varying(30),
	point3 character varying(30),
	point4 character varying(30),
	CONSTRAINT pk_devices_displays_id PRIMARY KEY (display_id, device_id)
);

SELECT setval('clients_id_seq', 10000, true);

COMMIT;
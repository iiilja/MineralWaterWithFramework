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

COMMIT;
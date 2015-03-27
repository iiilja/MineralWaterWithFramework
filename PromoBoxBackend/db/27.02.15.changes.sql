BEGIN;

ALTER TABLE users ADD COLUMN admin boolean DEFAULT FALSE;

ALTER TABLE clients ADD COLUMN created_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();
ALTER TABLE clients ADD COLUMN updated_dt TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

ALTER TABLE devices ADD COLUMN check_counter INTEGER NOT NULL DEFAULT 0;
ALTER TABLE devices ADD COLUMN state_period BIGINT NOT NULL DEFAULT 0;

CREATE TABLE users_devices_permissions
(
    id SERIAL NOT NULL,
    user_id INTEGER NOT NULL,
    client_id INTEGER NOT NULL,
    device_id INTEGER NOT NULL,
    permission_read BOOLEAN NOT NULL DEFAULT FALSE,
    permission_write BOOLEAN NOT NULL DEFAULT FALSE,
    created_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users_devices_permissions_id PRIMARY KEY (id)
);

CREATE TABLE users_campaigns_permissions
(
    id SERIAL NOT NULL,
    user_id INTEGER NOT NULL,
    client_id INTEGER NOT NULL,
    campaign_id INTEGER NOT NULL,
    permission_read BOOLEAN NOT NULL DEFAULT FALSE,
    permission_write BOOLEAN NOT NULL DEFAULT FALSE,
    created_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users_campaigns_permissions_id PRIMARY KEY (id)
);

CREATE TABLE versions
(
  id serial NOT NULL,
  version INTEGER NOT NULL,
  version_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  is_current BOOLEAN NOT NULL DEFAULT FALSE,
  description CHARACTER VARYING(255),
  CONSTRAINT pk_versions_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE error_log ADD COLUMN device_id INTEGER;

COMMIT;
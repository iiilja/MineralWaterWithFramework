BEGIN;

ALTER TABLE users ADD COLUMN admin boolean DEFAULT FALSE;

ALTER TABLE clients ADD COLUMN created_dt TIMESTAMP WITHOUT TIME ZONE DEFAULT now();
ALTER TABLE clients ADD COLUMN updated_dt TIMESTAMP WITHOUT TIME ZONE DEFAULT now();

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
  version character varying(50) NOT NULL,
  version_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  is_current BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT pk_versions_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

COMMIT;
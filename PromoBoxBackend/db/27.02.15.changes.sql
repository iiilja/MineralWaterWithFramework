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
    permission INTEGER NOT NULL,
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
    permission INTEGER NOT NULL,
    created_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_dt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users_campaigns_permissions_id PRIMARY KEY (id)
);

COMMIT;
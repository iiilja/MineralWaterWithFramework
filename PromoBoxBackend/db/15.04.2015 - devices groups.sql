CREATE TABLE devices_group
(
  id serial NOT NULL,
  client_id integer NOT NULL,
  name character varying(127) NOT NULL,
  CONSTRAINT pk_group_id PRIMARY KEY (id),
  CONSTRAINT fk_client_id FOREIGN KEY (client_id) REFERENCES clients(id)
);
CREATE TABLE devices_group_devices
(
  group_id integer NOT NULL,
  device_id integer NOT NULL,
  device_name character varying(255) NOT NULL,
  CONSTRAINT pk_devices_group_devices PRIMARY KEY (group_id,device_id)
);

ALTER TABLE devices DROP COLUMN check_counter;
ALTER TABLE devices ADD COLUMN on_off_counter integer DEFAULT 0 NOT NULL;
ALTER TABLE devices ADD COLUMN on_off_counter_check integer DEFAULT 0 NOT NULL;
ALTER TABLE devices ADD COLUMN on_off_check_number integer DEFAULT 1 NOT NULL;
--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.5
-- Dumped by pg_dump version 9.3.5
-- Started on 2014-11-04 14:41:02 MSK

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 184 (class 3079 OID 11793)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2040 (class 0 OID 0)
-- Dependencies: 184
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 183 (class 1259 OID 16470)
-- Name: ad_campaigns_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ad_campaigns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ad_campaigns_id_seq OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 182 (class 1259 OID 16467)
-- Name: ad_campaigns; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ad_campaigns (
    id integer DEFAULT nextval('ad_campaigns_id_seq'::regclass) NOT NULL,
    name character varying(255),
    client_id integer,
    status integer,
    sequence integer,
    start timestamp without time zone,
    finish timestamp without time zone,
    duration integer,
    update_dt timestamp without time zone,
    work_time_data text
);


ALTER TABLE public.ad_campaigns OWNER TO postgres;

--
-- TOC entry 175 (class 1259 OID 16418)
-- Name: campaigns_files; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE campaigns_files (
    id integer NOT NULL,
    ad_campaigns_id integer,
    client_id integer,
    file_id integer,
    file_type integer,
    order_id integer,
    status integer,
    size integer,
    created_dt timestamp without time zone,
    filename character varying(255)
);


ALTER TABLE public.campaigns_files OWNER TO postgres;

--
-- TOC entry 174 (class 1259 OID 16416)
-- Name: campaigns_files_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE campaigns_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.campaigns_files_id_seq OWNER TO postgres;

--
-- TOC entry 2041 (class 0 OID 0)
-- Dependencies: 174
-- Name: campaigns_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE campaigns_files_id_seq OWNED BY campaigns_files.id;


--
-- TOC entry 171 (class 1259 OID 16388)
-- Name: clients; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE clients (
    id integer NOT NULL,
    company_name character varying(255)
);


ALTER TABLE public.clients OWNER TO postgres;

--
-- TOC entry 170 (class 1259 OID 16386)
-- Name: clients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE clients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.clients_id_seq OWNER TO postgres;

--
-- TOC entry 2042 (class 0 OID 0)
-- Dependencies: 170
-- Name: clients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE clients_id_seq OWNED BY clients.id;


--
-- TOC entry 179 (class 1259 OID 16437)
-- Name: devices; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE devices (
    id integer NOT NULL,
    uuid character varying(255),
    client_id integer,
    status integer,
    description text,
    last_device_request_dt timestamp without time zone,
    free_space bigint,
    orientation integer,
    resolution integer,
    network_data text
);


ALTER TABLE public.devices OWNER TO postgres;

--
-- TOC entry 181 (class 1259 OID 16445)
-- Name: devices_campaigns; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE devices_campaigns (
    id integer NOT NULL,
    ad_campaigns_id integer,
    device_id integer,
    updated_dt timestamp without time zone
);


ALTER TABLE public.devices_campaigns OWNER TO postgres;

--
-- TOC entry 180 (class 1259 OID 16443)
-- Name: devices_campaigns_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE devices_campaigns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.devices_campaigns_id_seq OWNER TO postgres;

--
-- TOC entry 2043 (class 0 OID 0)
-- Dependencies: 180
-- Name: devices_campaigns_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE devices_campaigns_id_seq OWNED BY devices_campaigns.id;


--
-- TOC entry 178 (class 1259 OID 16435)
-- Name: devices_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE devices_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.devices_id_seq OWNER TO postgres;

--
-- TOC entry 2044 (class 0 OID 0)
-- Dependencies: 178
-- Name: devices_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE devices_id_seq OWNED BY devices.id;


--
-- TOC entry 177 (class 1259 OID 16426)
-- Name: files; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE files (
    id integer NOT NULL,
    filename character varying(255),
    path character varying(255),
    file_type bigint,
    created_dt timestamp without time zone,
    size bigint,
    client_id integer
);


ALTER TABLE public.files OWNER TO postgres;

--
-- TOC entry 176 (class 1259 OID 16424)
-- Name: files_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.files_id_seq OWNER TO postgres;

--
-- TOC entry 2045 (class 0 OID 0)
-- Dependencies: 176
-- Name: files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE files_id_seq OWNED BY files.id;


--
-- TOC entry 173 (class 1259 OID 16396)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    client_id integer,
    firstname character varying(255),
    surname character varying(255),
    email character varying(255),
    username character varying(255),
    password character varying(100),
    created_dt timestamp without time zone,
    last_login_dt timestamp without time zone,
    active boolean
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 172 (class 1259 OID 16394)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- TOC entry 2046 (class 0 OID 0)
-- Dependencies: 172
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- TOC entry 1907 (class 2604 OID 16421)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY campaigns_files ALTER COLUMN id SET DEFAULT nextval('campaigns_files_id_seq'::regclass);


--
-- TOC entry 1905 (class 2604 OID 16391)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY clients ALTER COLUMN id SET DEFAULT nextval('clients_id_seq'::regclass);


--
-- TOC entry 1909 (class 2604 OID 16440)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY devices ALTER COLUMN id SET DEFAULT nextval('devices_id_seq'::regclass);


--
-- TOC entry 1910 (class 2604 OID 16448)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY devices_campaigns ALTER COLUMN id SET DEFAULT nextval('devices_campaigns_id_seq'::regclass);


--
-- TOC entry 1908 (class 2604 OID 16429)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY files ALTER COLUMN id SET DEFAULT nextval('files_id_seq'::regclass);


--
-- TOC entry 1906 (class 2604 OID 16399)
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 1925 (class 2606 OID 16476)
-- Name: ad_campaigns_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ad_campaigns
    ADD CONSTRAINT ad_campaigns_pk PRIMARY KEY (id);


--
-- TOC entry 1917 (class 2606 OID 16423)
-- Name: campaigns_files_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY campaigns_files
    ADD CONSTRAINT campaigns_files_id_pk PRIMARY KEY (id);


--
-- TOC entry 1913 (class 2606 OID 16393)
-- Name: company_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY clients
    ADD CONSTRAINT company_id_pk PRIMARY KEY (id);


--
-- TOC entry 1923 (class 2606 OID 16450)
-- Name: devices_campaigns_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY devices_campaigns
    ADD CONSTRAINT devices_campaigns_id_pk PRIMARY KEY (id);


--
-- TOC entry 1921 (class 2606 OID 16442)
-- Name: devices_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY devices
    ADD CONSTRAINT devices_id_pk PRIMARY KEY (id);


--
-- TOC entry 1919 (class 2606 OID 16434)
-- Name: files_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY files
    ADD CONSTRAINT files_id_pk PRIMARY KEY (id);


--
-- TOC entry 1915 (class 2606 OID 16404)
-- Name: users_id_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_id_pk PRIMARY KEY (id);


--
-- TOC entry 2039 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2014-11-04 14:41:03 MSK

--
-- PostgreSQL database dump complete
--


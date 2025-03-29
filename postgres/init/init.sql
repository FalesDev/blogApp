DO $$
BEGIN 
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cyberblog') THEN
        CREATE DATABASE cyberblog;
END IF;
END $$;


ALTER DATABASE cyberblog OWNER TO postgres;
GRANT ALL PRIVILEGES ON DATABASE cyberblog TO postgres;
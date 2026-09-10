-- Runs once, on first initialization of a fresh postgres_data volume (see
-- docker-entrypoint-initdb.d in the postgres image docs). The app's connection pool is
-- configured (spring.datasource.hikari.schema, in src/main/resources/application.properties)
-- to use the "eva" schema, so it needs to exist before the app starts.
CREATE SCHEMA IF NOT EXISTS eva;

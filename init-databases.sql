-- BiblioTech PostgreSQL Database Initialization Script
-- Run this script as a PostgreSQL superuser (e.g., postgres) to create all required databases

-- Create databases for each microservice
CREATE DATABASE bibliotech_auth;
CREATE DATABASE bibliotech_books;
CREATE DATABASE bibliotech_rentals;
CREATE DATABASE bibliotech_fines;

-- Optional: Create a dedicated application user
-- CREATE USER bibliotech_user WITH PASSWORD 'bibliotech_pass';
-- GRANT ALL PRIVILEGES ON DATABASE bibliotech_auth TO bibliotech_user;
-- GRANT ALL PRIVILEGES ON DATABASE bibliotech_books TO bibliotech_user;
-- GRANT ALL PRIVILEGES ON DATABASE bibliotech_rentals TO bibliotech_user;
-- GRANT ALL PRIVILEGES ON DATABASE bibliotech_fines TO bibliotech_user;

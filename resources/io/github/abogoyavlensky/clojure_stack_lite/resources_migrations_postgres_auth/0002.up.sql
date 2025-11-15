-- Create users table with authentication-related fields
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    email CITEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

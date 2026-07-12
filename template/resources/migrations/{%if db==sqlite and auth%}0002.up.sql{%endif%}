-- Create users table with authentication-related fields
CREATE TABLE IF NOT EXISTS "user" (
    id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create an index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_email ON "user" (email);

-- Create users table with authentication-related fields
CREATE TABLE IF NOT EXISTS "user" (
    id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,
    email TEXT NOT NULL COLLATE NOCASE UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

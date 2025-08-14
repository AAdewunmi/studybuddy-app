
-- ========================
-- PostgreSQL Schema: StudyBuddy
-- ========================

CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('USER', 'ADMIN', 'SYSTEM_ADMIN')) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "group" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE membership (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    group_id INT NOT NULL REFERENCES "group"(id) ON DELETE CASCADE,
    role_in_group VARCHAR(20) CHECK (role_in_group IN ('MEMBER', 'ADMIN')) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, group_id)
);

CREATE TABLE habit (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    frequency VARCHAR(20) CHECK (frequency IN ('DAILY', 'WEEKLY')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE habit_checkin (
    id SERIAL PRIMARY KEY,
    habit_id INT NOT NULL REFERENCES habit(id) ON DELETE CASCADE,
    checkin_date DATE NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (habit_id, checkin_date)
);

CREATE TABLE study_session (
    id SERIAL PRIMARY KEY,
    group_id INT NOT NULL REFERENCES "group"(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_by INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE
);

-- Optional indexes for faster queries
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_group_name ON "group"(name);
CREATE INDEX idx_habit_user ON habit(user_id);
CREATE INDEX idx_session_group ON study_session(group_id);

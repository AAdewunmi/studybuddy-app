
-- ========================
-- Sample Data: StudyBuddy
-- ========================

-- Insert Users
INSERT INTO "user" (name, email, password_hash, role) VALUES
('Alice Johnson', 'alice@example.com', 'hashed_pw_1', 'USER'),
('Bob Smith', 'bob@example.com', 'hashed_pw_2', 'ADMIN'),
('Carol Lee', 'carol@example.com', 'hashed_pw_3', 'USER');

-- Insert Groups
INSERT INTO "group" (name, description, created_by) VALUES
('Math Study Group', 'Collaborative group for algebra and calculus practice', 1),
('Java Bootcamp', 'Group for learning Java and Spring Boot', 2);

-- Insert Memberships
INSERT INTO membership (user_id, group_id, role_in_group) VALUES
(1, 1, 'ADMIN'),
(2, 1, 'MEMBER'),
(3, 1, 'MEMBER'),
(2, 2, 'ADMIN'),
(1, 2, 'MEMBER');

-- Insert Habits
INSERT INTO habit (user_id, name, frequency) VALUES
(1, 'Review math notes', 'DAILY'),
(1, 'Practice Java coding', 'DAILY'),
(2, 'Read Spring Boot docs', 'WEEKLY'),
(3, 'Complete coding challenges', 'DAILY');

-- Insert Habit Check-ins
INSERT INTO habit_checkin (habit_id, checkin_date, status) VALUES
(1, '2025-08-10', TRUE),
(1, '2025-08-11', TRUE),
(2, '2025-08-11', FALSE),
(3, '2025-08-09', TRUE),
(4, '2025-08-11', TRUE);

-- Insert Study Sessions
INSERT INTO study_session (group_id, title, description, start_time, end_time, created_by) VALUES
(1, 'Algebra Practice', 'Solving equations and inequalities', '2025-08-12 10:00', '2025-08-12 12:00', 1),
(2, 'Spring Boot Workshop', 'Introduction to Spring Boot basics', '2025-08-13 14:00', '2025-08-13 16:00', 2);

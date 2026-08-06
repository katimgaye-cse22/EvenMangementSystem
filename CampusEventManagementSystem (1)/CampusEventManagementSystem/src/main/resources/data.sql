-- ============================================================
-- Seed data — runs on startup (spring.sql.init.mode=always).
-- INSERT IGNORE makes this safe to re-run: the unique constraint
-- on `email` prevents duplicate rows on every app restart.
-- ============================================================

-- Seed administrator account.
-- Email:    admin@campus.edu
-- Password: Admin@1234   (bcrypt hash below — change this in production!)
INSERT IGNORE INTO users (full_name, student_id, email, password, role, enabled, created_at)
VALUES (
    'System Administrator',
    'ADMIN-0001',
    'admin@campus.edu',
    '$2b$10$sXfLrFsTduZEfjbilb5QYeCHZLXDiE/Hh9hlNKj0dt6SzopvtxGGK',
    'ADMIN',
    TRUE,
    NOW()
);

-- ============================================================
-- Campus Event Management System - Schema
-- ============================================================
-- Note: with spring.jpa.hibernate.ddl-auto=update, Hibernate will
-- create/update these tables automatically from the entities.
-- This file is kept as an explicit reference and for manual setup
-- (set spring.sql.init.mode=always to run it on startup instead).

CREATE DATABASE IF NOT EXISTS campus_event_db;
USE campus_event_db;

-- ------------------------------------------------------------
-- USERS  (students + administrators)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
                                     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     full_name       VARCHAR(100)  NOT NULL,
    student_id      VARCHAR(30)   NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    password        VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL DEFAULT 'STUDENT',
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      DATETIME      NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_student_id UNIQUE (student_id)
    );


-- ------------------------------------------------------------
-- EVENTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
                                      id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      title               VARCHAR(150)  NOT NULL,
    description         TEXT,
    category            VARCHAR(50)   NOT NULL,
    venue               VARCHAR(150)  NOT NULL,
    event_date          DATE          NOT NULL,
    start_time          TIME          NOT NULL,
    end_time            TIME          NOT NULL,
    capacity            INT           NOT NULL,
    registered_count    INT           NOT NULL DEFAULT 0,
    version             BIGINT        NOT NULL DEFAULT 0,
    created_by          BIGINT        NOT NULL,
    created_at          DATETIME      NOT NULL,
    updated_at          DATETIME,

    CONSTRAINT fk_event_created_by FOREIGN KEY (created_by) REFERENCES users (id)
    );


-- ------------------------------------------------------------
-- REGISTRATIONS  (join table: student <-> event, with status)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registrations (
                                             id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id         BIGINT        NOT NULL,
                                             event_id        BIGINT        NOT NULL,
                                             status          VARCHAR(20)   NOT NULL DEFAULT 'REGISTERED',
    registered_at   DATETIME      NOT NULL,
    cancelled_at    DATETIME,

    CONSTRAINT fk_registration_user  FOREIGN KEY (user_id)  REFERENCES users (id),
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT uk_user_event UNIQUE (user_id, event_id)
    );
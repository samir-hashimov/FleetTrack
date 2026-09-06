CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       username    VARCHAR(100) NOT NULl,
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       role        VARCHAR(50)  NOT NULL,
                       created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicles (
                          id             BIGSERIAL PRIMARY KEY,
                          make           VARCHAR(100) NOT NULL,
                          model          VARCHAR(100) NOT NULL,
                          year           INTEGER      NOT NULL,
                          license_plate  VARCHAR(20)  NOT NULL UNIQUE,
                          status         VARCHAR(50)  NOT NULL,
                          latitude       DOUBLE PRECISION,
                          longitude      DOUBLE PRECISION,
                          created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          CONSTRAINT chk_vehicle_year CHECK (year >= 1900 AND year <= 2100)
    );

CREATE TABLE drivers (
                         id              BIGSERIAL PRIMARY KEY,
                         first_name      VARCHAR(100) NOT NULL,
                         last_name       VARCHAR(100) NOT NULL,
                         license_number  VARCHAR(50)  NOT NULL UNIQUE,
                         email           VARCHAR(255),
                         phone           VARCHAR(30),
                         status          VARCHAR(20)  NOT NULL,
                         user_id         BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL,
                         vehicle_id      BIGINT UNIQUE REFERENCES vehicles(id) ON DELETE SET NULL,
                         created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE maintenance_logs (
                                  id              BIGSERIAL PRIMARY KEY,
                                  vehicle_id      BIGINT       NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                  description     TEXT         NOT NULL,
                                  scheduled_date  DATE         NOT NULL,
                                  is_completed    BOOLEAN      NOT NULL DEFAULT FALSE,
                                  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_year ON vehicles(year);
CREATE INDEX idx_maintenance_scheduled_date ON maintenance_logs(scheduled_date);
CREATE INDEX idx_maintenance_is_completed ON maintenance_logs(is_completed);

-- Default users (Parol artiq 123456 olacaq)
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@fleettrack.com', '$2a$10$cYLM.qoXpeAzcZhJ3oXRLu9Slkb61LHyWW5qJ4QKvHEMhaxZ5qCPi', 'ADMIN');

INSERT INTO users (username, email, password, role)
VALUES ('fleetmanager', 'manager@fleettrack.com', '$2a$10$cYLM.qoXpeAzcZhJ3oXRLu9Slkb61LHyWW5qJ4QKvHEMhaxZ5qCPi', 'FLEET_MANAGER');

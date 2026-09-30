CREATE TABLE IF NOT EXISTS insurance_regimes (
  id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eps (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eps_plans (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  eps_id BIGINT UNSIGNED NOT NULL,
  regime_id SMALLINT UNSIGNED NOT NULL,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(150) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_eps_plans_identity UNIQUE (eps_id, regime_id, code),
  CONSTRAINT fk_s4_eps_plan_eps FOREIGN KEY (eps_id) REFERENCES eps(id),
  CONSTRAINT fk_s4_eps_plan_regime FOREIGN KEY (regime_id) REFERENCES insurance_regimes(id)
) ENGINE=InnoDB;

INSERT IGNORE INTO insurance_regimes (code, name) VALUES
  ('CONTRIBUTIVO', 'Contributivo'),
  ('SUBSIDIADO', 'Subsidiado'),
  ('PARTICULAR', 'Particular');

CREATE TABLE IF NOT EXISTS user_insurance_affiliations (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  plan_id BIGINT UNSIGNED NOT NULL,
  membership_number VARCHAR(60) NOT NULL,
  valid_from DATE NOT NULL,
  valid_to DATE,
  current_user_id BIGINT UNSIGNED AS (IF(valid_to IS NULL, user_id, NULL)) STORED,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_s4_affiliation_member UNIQUE (plan_id, membership_number),
  CONSTRAINT uq_s4_affiliation_current UNIQUE (current_user_id),
  CONSTRAINT ck_s4_affiliation_dates CHECK (valid_to IS NULL OR valid_to >= valid_from),
  CONSTRAINT fk_s4_affiliation_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_s4_affiliation_plan FOREIGN KEY (plan_id) REFERENCES eps_plans(id)
) ENGINE=InnoDB;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='appointments' AND column_name='insurance_affiliation_id')=0,
  'ALTER TABLE appointments ADD COLUMN insurance_affiliation_id BIGINT UNSIGNED NULL', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='appointments' AND column_name='updated_at')=0,
  'ALTER TABLE appointments ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema=DATABASE() AND table_name='appointments' AND column_name='insurance_affiliation_id' AND referenced_table_name IS NOT NULL)=0,
  'ALTER TABLE appointments ADD CONSTRAINT fk_s4_appointment_affiliation FOREIGN KEY (insurance_affiliation_id) REFERENCES user_insurance_affiliations(id)', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='appointments' AND index_name='ix_s4_appointments_patient_date')=0,
  'CREATE INDEX ix_s4_appointments_patient_date ON appointments(patient_user_id,scheduled_start_at)', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='appointments' AND index_name='ix_s4_appointments_professional_date')=0,
  'CREATE INDEX ix_s4_appointments_professional_date ON appointments(professional_id,scheduled_start_at)', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

CREATE TABLE IF NOT EXISTS reschedule_request_statuses (
  id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL,
  is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

INSERT IGNORE INTO reschedule_request_statuses (code, name, is_terminal) VALUES
  ('PENDING', 'Pendiente', FALSE),
  ('APPROVED', 'Aprobada', TRUE),
  ('REJECTED', 'Rechazada', TRUE),
  ('CANCELLED', 'Cancelada', TRUE);

CREATE TABLE IF NOT EXISTS reschedule_requests (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT UNSIGNED NOT NULL,
  requested_by_user_id BIGINT UNSIGNED NOT NULL,
  requested_location_id SMALLINT UNSIGNED NOT NULL,
  status_id SMALLINT UNSIGNED NOT NULL,
  requested_start_at DATETIME NOT NULL,
  requested_end_at DATETIME NOT NULL,
  decision_reason VARCHAR(500),
  decided_by_user_id BIGINT UNSIGNED,
  decided_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_s4_reschedule_time CHECK (requested_end_at > requested_start_at),
  CONSTRAINT fk_s4_reschedule_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
  CONSTRAINT fk_s4_reschedule_requester FOREIGN KEY (requested_by_user_id) REFERENCES users(id),
  CONSTRAINT fk_s4_reschedule_location FOREIGN KEY (requested_location_id) REFERENCES locations(id),
  CONSTRAINT fk_s4_reschedule_status FOREIGN KEY (status_id) REFERENCES reschedule_request_statuses(id),
  CONSTRAINT fk_s4_reschedule_decider FOREIGN KEY (decided_by_user_id) REFERENCES users(id),
  INDEX ix_s4_reschedule_status (status_id, created_at)
) ENGINE=InnoDB;

ALTER TABLE slot_reservations MODIFY COLUMN appointment_id BIGINT UNSIGNED NULL;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='slot_reservations' AND column_name='reschedule_request_id')=0,
  'ALTER TABLE slot_reservations ADD COLUMN reschedule_request_id BIGINT UNSIGNED NULL', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='slot_reservations' AND constraint_type='CHECK')=0,
  'ALTER TABLE slot_reservations ADD CONSTRAINT ck_s4_slot_reservation_owner CHECK ((appointment_id IS NOT NULL) <> (reschedule_request_id IS NOT NULL))', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema=DATABASE() AND table_name='slot_reservations' AND column_name='reschedule_request_id' AND referenced_table_name IS NOT NULL)=0,
  'ALTER TABLE slot_reservations ADD CONSTRAINT fk_s4_slot_reservation_reschedule FOREIGN KEY (reschedule_request_id) REFERENCES reschedule_requests(id) ON DELETE CASCADE', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

SET @s4_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='slot_reservations' AND index_name='ix_s4_slot_reservation_reschedule')=0,
  'CREATE INDEX ix_s4_slot_reservation_reschedule ON slot_reservations(reschedule_request_id)', 'SELECT 1');
PREPARE s4_stmt FROM @s4_ddl;
EXECUTE s4_stmt;
DEALLOCATE PREPARE s4_stmt;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  used_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_s4_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX ix_s4_password_reset_active (user_id, expires_at, used_at)
) ENGINE=InnoDB;
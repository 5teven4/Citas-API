CREATE TABLE IF NOT EXISTS locations (
  id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL, address VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL, appointment_duration_minutes SMALLINT UNSIGNED NOT NULL,
  is_general BOOLEAN NOT NULL DEFAULT FALSE, requires_admin_approval BOOLEAN NOT NULL DEFAULT TRUE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT ck_s3_specialty_duration CHECK (appointment_duration_minutes IN (30, 60))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professionals (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL UNIQUE,
  professional_code VARCHAR(40) NOT NULL UNIQUE, license_number VARCHAR(60) NOT NULL UNIQUE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_s3_professional_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_specialties (
  professional_id BIGINT UNSIGNED NOT NULL, specialty_id SMALLINT UNSIGNED NOT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (professional_id, specialty_id),
  CONSTRAINT fk_s3_ps_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
  CONSTRAINT fk_s3_ps_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_locations (
  professional_id BIGINT UNSIGNED NOT NULL, location_id SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (professional_id, location_id),
  CONSTRAINT fk_s3_pl_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
  CONSTRAINT fk_s3_pl_location FOREIGN KEY (location_id) REFERENCES locations(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointment_statuses (
  id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL, is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS availability_blocks (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, professional_id BIGINT UNSIGNED NOT NULL,
  location_id SMALLINT UNSIGNED NOT NULL, available_date DATE NOT NULL, start_time TIME NOT NULL,
  end_time TIME NOT NULL, is_active BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_s3_block_range CHECK (end_time > start_time),
  CONSTRAINT fk_s3_block_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
  CONSTRAINT fk_s3_block_location FOREIGN KEY (location_id) REFERENCES locations(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_slots (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, availability_block_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME NOT NULL, end_at DATETIME NOT NULL,
  CONSTRAINT uq_s3_slot_start UNIQUE (availability_block_id, start_at),
  CONSTRAINT fk_s3_slot_block FOREIGN KEY (availability_block_id) REFERENCES availability_blocks(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, patient_user_id BIGINT UNSIGNED NOT NULL,
  professional_id BIGINT UNSIGNED NOT NULL, location_id SMALLINT UNSIGNED NOT NULL,
  specialty_id SMALLINT UNSIGNED NOT NULL, status_id SMALLINT UNSIGNED NOT NULL,
  scheduled_start_at DATETIME NOT NULL, scheduled_end_at DATETIME NOT NULL,
  duration_minutes SMALLINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_s3_appointment_patient FOREIGN KEY (patient_user_id) REFERENCES users(id),
  CONSTRAINT fk_s3_appointment_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
  CONSTRAINT fk_s3_appointment_location FOREIGN KEY (location_id) REFERENCES locations(id),
  CONSTRAINT fk_s3_appointment_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  CONSTRAINT fk_s3_appointment_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id)
) ENGINE=InnoDB;

ALTER TABLE appointments ADD COLUMN rejection_reason VARCHAR(500);

CREATE TABLE IF NOT EXISTS slot_reservations (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, professional_slot_id BIGINT UNSIGNED NOT NULL UNIQUE,
  appointment_id BIGINT UNSIGNED NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_s3_reservation_slot FOREIGN KEY (professional_slot_id) REFERENCES professional_slots(id) ON DELETE CASCADE,
  CONSTRAINT fk_s3_reservation_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointment_status_history (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, appointment_id BIGINT UNSIGNED NOT NULL,
  status_id SMALLINT UNSIGNED NOT NULL, changed_by_user_id BIGINT UNSIGNED,
  change_source ENUM('SYSTEM','USER','ADMIN') NOT NULL, reason VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_s3_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
  CONSTRAINT fk_s3_history_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id),
  CONSTRAINT fk_s3_history_actor FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

INSERT IGNORE INTO locations (code, name, address) VALUES
 ('HIC', 'Hospital Internacional de Colombia (HIC)', 'Sede ficticia de laboratorio'),
 ('ICV', 'Instituto Cardiovascular (ICV)', 'Sede ficticia de laboratorio');
INSERT IGNORE INTO specialties (code, name, appointment_duration_minutes, is_general, requires_admin_approval) VALUES
 ('GENERAL_MEDICINE', 'Medicina General', 30, TRUE, FALSE),
 ('CARDIOLOGY', 'Cardiología', 60, FALSE, TRUE);
INSERT IGNORE INTO appointment_statuses (code, name, is_terminal) VALUES
 ('REQUESTED', 'Solicitada', FALSE), ('APPROVED', 'Aprobada', FALSE),
 ('REJECTED', 'Rechazada', TRUE), ('CANCELLED', 'Cancelada', TRUE),
 ('COMPLETED', 'Completada', TRUE), ('NO_SHOW', 'No asistió', TRUE);

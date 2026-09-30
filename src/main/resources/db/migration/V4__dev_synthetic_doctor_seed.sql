-- Datos sintéticos para demo local y pruebas manuales. No contiene PII real.
INSERT IGNORE INTO users
  (first_name, last_name, document_type, document_number, email, phone, password_hash)
VALUES
  ('Laura', 'Gómez', 'CC', '900000001', 'laura.gomez.demo@fcv.local', '3000000001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
  ('Andrés', 'Rojas', 'CC', '900000002', 'andres.rojas.demo@fcv.local', '3000000002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
  ('Sofía', 'Martínez', 'CC', '900000003', 'sofia.martinez.demo@fcv.local', '3000000003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'PROFESSIONAL'
WHERE u.email IN ('laura.gomez.demo@fcv.local', 'andres.rojas.demo@fcv.local', 'sofia.martinez.demo@fcv.local');

INSERT IGNORE INTO professionals (user_id, professional_code, license_number)
SELECT id, 'DOC-LAURA', 'MAT-DEMO-001' FROM users WHERE email = 'laura.gomez.demo@fcv.local'
UNION ALL SELECT id, 'DOC-ANDRES', 'MAT-DEMO-002' FROM users WHERE email = 'andres.rojas.demo@fcv.local'
UNION ALL SELECT id, 'DOC-SOFIA', 'MAT-DEMO-003' FROM users WHERE email = 'sofia.martinez.demo@fcv.local';

INSERT IGNORE INTO professional_specialties (professional_id, specialty_id, is_primary)
SELECT p.id, s.id, TRUE FROM professionals p JOIN users u ON u.id = p.user_id JOIN specialties s ON s.code = 'GENERAL_MEDICINE'
WHERE u.email IN ('laura.gomez.demo@fcv.local', 'andres.rojas.demo@fcv.local');
INSERT IGNORE INTO professional_specialties (professional_id, specialty_id, is_primary)
SELECT p.id, s.id, TRUE FROM professionals p JOIN users u ON u.id = p.user_id JOIN specialties s ON s.code = 'CARDIOLOGY'
WHERE u.email = 'sofia.martinez.demo@fcv.local';

INSERT IGNORE INTO professional_locations (professional_id, location_id)
SELECT p.id, l.id FROM professionals p JOIN users u ON u.id = p.user_id CROSS JOIN locations l
WHERE u.email IN ('laura.gomez.demo@fcv.local', 'andres.rojas.demo@fcv.local') AND l.code IN ('HIC', 'ICV');
INSERT IGNORE INTO professional_locations (professional_id, location_id)
SELECT p.id, l.id FROM professionals p JOIN users u ON u.id = p.user_id JOIN locations l ON l.code = 'ICV'
WHERE u.email = 'sofia.martinez.demo@fcv.local';

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time)
SELECT p.id, pl.location_id, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '08:00:00', '12:00:00'
FROM professionals p JOIN users u ON u.id = p.user_id JOIN professional_locations pl ON pl.professional_id = p.id
WHERE u.email IN ('laura.gomez.demo@fcv.local', 'andres.rojas.demo@fcv.local');
INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time)
SELECT p.id, pl.location_id, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:00:00', '17:00:00'
FROM professionals p JOIN users u ON u.id = p.user_id JOIN professional_locations pl ON pl.professional_id = p.id
WHERE u.email = 'sofia.martinez.demo@fcv.local';

INSERT INTO professional_slots (availability_block_id, start_at, end_at)
SELECT b.id, TIMESTAMP(b.available_date, times.slot_time), TIMESTAMP(b.available_date, ADDTIME(times.slot_time, '00:30:00'))
FROM availability_blocks b
JOIN (SELECT '08:00:00' AS slot_time UNION ALL SELECT '08:30:00' UNION ALL SELECT '09:00:00' UNION ALL SELECT '09:30:00'
      UNION ALL SELECT '10:00:00' UNION ALL SELECT '10:30:00' UNION ALL SELECT '11:00:00' UNION ALL SELECT '11:30:00'
      UNION ALL SELECT '14:00:00' UNION ALL SELECT '14:30:00' UNION ALL SELECT '15:00:00' UNION ALL SELECT '15:30:00'
      UNION ALL SELECT '16:00:00' UNION ALL SELECT '16:30:00') times
WHERE b.available_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY)
  AND times.slot_time < b.end_time;

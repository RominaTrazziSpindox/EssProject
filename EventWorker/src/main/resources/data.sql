-- Reset development data in a predictable way.
TRUNCATE TABLE attendees, campaigns RESTART IDENTITY CASCADE;

-- Insert campaigns with different sizes and patterns.
INSERT INTO campaigns (id, campaign_id, sub_campaign_id) VALUES
(1, 'C-2026001', 'SC-NORTH'),
(2, 'C-2026002', NULL),
(3, 'C-2026003', 'SC-FAMILY'),
(4, 'C-2026004', NULL),
(5, 'C-2026005', 'SC-SENIOR'),
(6, 'C-2026006', NULL),
(7, 'C-2026007', 'SC-DATA'),
(8, 'C-2026008', NULL);

-- Campaign 1: small campaign, one companion.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('1001001', 'Matteo', 'Ricci', '1985-05-12', '1001001', false, 'QR-C2026001-01', 1),
('laura.ferretti@email.it', 'Laura', 'Ferretti', '1991-08-22', '1001002', false, 'QR-C2026001-02', 1),
(NULL, 'Andrea', 'Greco', NULL, '1001002', true, 'QR-C2026001-03', 1);

-- Campaign 2: very small campaign, no companion.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('2002001', 'Aurora', 'Conti', '1999-07-14', '2002001', false, 'QR-C2026002-01', 2),
('2002002', 'Alessandro', 'De Luca', '1978-08-01', '2002002', false, 'QR-C2026002-02', 2);

-- Campaign 3: family-oriented campaign with several companions.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('3003001', 'Giulia', 'Marino', '1990-11-23', '3003001', false, 'QR-C2026003-01', 3),
(NULL, 'Emma', 'Marino', NULL, '3003001', true, 'QR-C2026003-02', 3),
('3003002', 'Davide', 'Serra', '1982-04-10', '3003002', false, 'QR-C2026003-03', 3),
(NULL, 'Chiara', 'Serra', NULL, '3003002', true, 'QR-C2026003-04', 3),
('francesca.bellini@email.it', 'Francesca', 'Bellini', '1988-02-17', '3003003', false, 'QR-C2026003-05', 3),
(NULL, 'Tommaso', 'Bellini', NULL, '3003003', true, 'QR-C2026003-06', 3);

-- Campaign 4: large campaign with mixed attendee profiles.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('4004001', 'Luca', 'Bianchi', '1975-01-20', '4004001', false, 'QR-C2026004-01', 4),
('marta.vitali@email.it', 'Marta', 'Vitali', '1987-03-05', '4004002', false, 'QR-C2026004-02', 4),
(NULL, 'Sofia', 'Vitali', NULL, '4004002', true, 'QR-C2026004-03', 4),
('4004003', 'Elisa', 'Rinaldi', '1993-12-11', '4004003', false, 'QR-C2026004-04', 4),
('4004004', 'Simone', 'Caruso', '1981-09-18', '4004004', false, 'QR-C2026004-05', 4),
(NULL, 'Noemi', 'Caruso', NULL, '4004004', true, 'QR-C2026004-06', 4),
('andrea.moretti@email.it', 'Andrea', 'Moretti', '1996-06-07', '4004005', false, 'QR-C2026004-07', 4),
(NULL, 'Giorgia', 'Moretti', NULL, '4004005', true, 'QR-C2026004-08', 4);

-- Campaign 5: senior-heavy campaign, no companion.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('5005001', 'Paolo', 'Neri', '1962-05-09', '5005001', false, 'QR-C2026005-01', 5),
('5005002', 'Silvia', 'Gallo', '1965-10-30', '5005002', false, 'QR-C2026005-02', 5),
('5005003', 'Roberto', 'Fontana', '1959-02-14', '5005003', false, 'QR-C2026005-03', 5),
('5005004', 'Daniela', 'Parisi', '1968-07-01', '5005004', false, 'QR-C2026005-04', 5);

-- Campaign 6: younger audience with one companion.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('6006001', 'Martina', 'Leone', '2001-01-15', '6006001', false, 'QR-C2026006-01', 6),
('6006002', 'Federico', 'Romano', '1998-11-19', '6006002', false, 'QR-C2026006-02', 6),
('6006003', 'Sara', 'Greco', '2003-04-08', '6006003', false, 'QR-C2026006-03', 6),
('6006004', 'Nicolò', 'Pellegrini', '1997-06-25', '6006004', false, 'QR-C2026006-04', 6),
(NULL, 'Alice', 'Pellegrini', NULL, '6006004', true, 'QR-C2026006-05', 6);

-- Campaign 7: data-quality oriented campaign with more missing fields.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('7007001', 'Valentina', 'Testa', '1989-09-09', '7007001', false, 'QR-C2026007-01', 7),
(NULL, 'Marco', 'Testa', NULL, '7007001', true, 'QR-C2026007-02', 7),
('claudio.esposito@email.it', 'Claudio', 'Esposito', '1977-03-27', '7007002', false, 'QR-C2026007-03', 7),
(NULL, 'Anna', 'Esposito', NULL, '7007002', true, 'QR-C2026007-04', 7),
('7007003', 'Beatrice', 'Lombardi', '1994-12-03', '7007003', false, 'QR-C2026007-05', 7),
('7007004', 'Enrico', 'Colombo', '1983-01-16', '7007004', false, 'QR-C2026007-06', 7),
(NULL, 'Michela', 'Colombo', NULL, '7007004', true, 'QR-C2026007-07', 7);

-- Campaign 8: single-attendee campaign.
INSERT INTO attendees (cn, first_name, last_name, birth_date, partner_id, is_companion, qr_code, campaign_id_rif) VALUES
('8008001', 'Irene', 'Giordano', '1992-05-21', '8008001', false, 'QR-C2026008-01', 8);
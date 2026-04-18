-- V2: Insert test and admin accounts for testers
INSERT IGNORE INTO users (email, password, name, phone_number, gender, role, is_active)
VALUES 
('visitor@sparta-msa.com', '$2a$10$L1rLPXuTB/RmhVwm7IdcLeLd93siSnP0qtEQK.1o32C4JZdIO8fe.', '테스터', '010-1234-5678', 'MALE', 'USER', true),
('admin@sparta-msa.com', '$2a$10$L1rLPXuTB/RmhVwm7IdcLeLd93siSnP0qtEQK.1o32C4JZdIO8fe.', '관리자', '010-9876-5432', 'FEMALE', 'ADMIN', true);

-- V1.0.2 - Seed test users for development

-- Default centre
INSERT INTO centre (id, name, active) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Wishaw YMCA', TRUE);

-- Test users (password_hash = bcrypt of 'password123', cost=10)
-- Role: ADMIN
INSERT INTO app_user (id, centre_id, username, password_hash, role, active) VALUES
    ('00000000-0000-0000-0001-000000000001',
     '00000000-0000-0000-0000-000000000001',
     'admin1',
     '$2a$10$.WTQbCQKE8iS64HQRX/DSOJ1MOnXd6qQujPlvkTYu96rCb7GS88ee',
     'ADMIN',
     TRUE);

-- Role: STAFF
INSERT INTO app_user (id, centre_id, username, password_hash, role, active) VALUES
    ('00000000-0000-0000-0002-000000000001',
     '00000000-0000-0000-0000-000000000001',
     'coach1',
     '$2a$10$.WTQbCQKE8iS64HQRX/DSOJ1MOnXd6qQujPlvkTYu96rCb7GS88ee',
     'COACH',
     TRUE);

-- Role: PLAYER
INSERT INTO app_user (id, centre_id, username, password_hash, role, active) VALUES
    ('00000000-0000-0000-0003-000000000001',
     '00000000-0000-0000-0000-000000000001',
     'player1',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'PLAYER',
     TRUE);

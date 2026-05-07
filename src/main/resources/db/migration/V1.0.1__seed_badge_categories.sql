-- V1.0.1 - Seed reference data

-- Badge Categories
INSERT INTO badge_category (id, display_name, description) VALUES
    (RANDOM_UUID(), 'Game Mastery', 'Mastery of in-game skills and mechanics');
INSERT INTO badge_category (id, display_name, description) VALUES
    (RANDOM_UUID(), 'Teamwork', 'Collaboration and team-based achievements');
INSERT INTO badge_category (id, display_name, description) VALUES
    (RANDOM_UUID(), 'Esports Citizen', 'Good sportsmanship and community behaviour');
INSERT INTO badge_category (id, display_name, description) VALUES
    (RANDOM_UUID(), 'Personal Development', 'Growth in personal and soft skills');
INSERT INTO badge_category (id, display_name, description) VALUES
    (RANDOM_UUID(), 'Digital Skills', 'Technical and digital literacy achievements');

-- Level Definitions (global, not per-category)
INSERT INTO level_definition (id, name, min_xp, max_xp) VALUES
    (RANDOM_UUID(), 'Bronze', 0, 30);
INSERT INTO level_definition (id, name, min_xp, max_xp) VALUES
    (RANDOM_UUID(), 'Silver', 31, 70);
INSERT INTO level_definition (id, name, min_xp, max_xp) VALUES
    (RANDOM_UUID(), 'Gold', 71, 120);
INSERT INTO level_definition (id, name, min_xp, max_xp) VALUES
    (RANDOM_UUID(), 'Platinum', 121, 999999);

-- Games
INSERT INTO game (id, display_name, active) VALUES
    (RANDOM_UUID(), 'Minecraft', TRUE);
INSERT INTO game (id, display_name, active) VALUES
    (RANDOM_UUID(), 'Rocket League', TRUE);
INSERT INTO game (id, display_name, active) VALUES
    (RANDOM_UUID(), 'Fortnite', TRUE);
INSERT INTO game (id, display_name, active) VALUES
    (RANDOM_UUID(), 'Generic', TRUE);

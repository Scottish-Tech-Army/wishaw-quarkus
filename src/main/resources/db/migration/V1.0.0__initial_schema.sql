-- V1.0.0 - Initial schema for Wishaw YMCA Esports Login App

CREATE TABLE metadata (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    icon        VARCHAR(255),
    link        VARCHAR(500)
);

CREATE TABLE centre (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE app_user (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    centre_id       UUID         NOT NULL,
    metadata_id     UUID,
    parent_id       UUID,
    username        VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_user_centre FOREIGN KEY (centre_id) REFERENCES centre(id),
    CONSTRAINT fk_user_metadata FOREIGN KEY (metadata_id) REFERENCES metadata(id),
    CONSTRAINT fk_user_parent FOREIGN KEY (parent_id) REFERENCES app_user(id)
);

CREATE INDEX idx_app_user_centre_id ON app_user(centre_id);
CREATE INDEX idx_app_user_username ON app_user(username);

CREATE TABLE badge_category (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    display_name    VARCHAR(255) NOT NULL,
    description     CLOB
);

CREATE TABLE level_definition (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    min_xp      INT NOT NULL,
    max_xp      INT NOT NULL
);

CREATE TABLE game (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    display_name    VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE module (
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    metadata_id     UUID,
    display_name    VARCHAR(255) NOT NULL,
    description     CLOB,
    game_id         UUID NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_module_metadata FOREIGN KEY (metadata_id) REFERENCES metadata(id),
    CONSTRAINT fk_module_game FOREIGN KEY (game_id) REFERENCES game(id)
);

CREATE INDEX idx_module_game_id ON module(game_id);

CREATE TABLE challenge (
    id                  UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    module_id           UUID NOT NULL,
    metadata_id         UUID,
    display_name        VARCHAR(255) NOT NULL,
    description         CLOB,
    badge_category_id   UUID NOT NULL,
    xp_value            INT NOT NULL,
    CONSTRAINT fk_challenge_module FOREIGN KEY (module_id) REFERENCES module(id),
    CONSTRAINT fk_challenge_metadata FOREIGN KEY (metadata_id) REFERENCES metadata(id),
    CONSTRAINT fk_challenge_badge_category FOREIGN KEY (badge_category_id) REFERENCES badge_category(id)
);

CREATE INDEX idx_challenge_module_id ON challenge(module_id);
CREATE INDEX idx_challenge_badge_category ON challenge(badge_category_id);

CREATE TABLE challenge_submission (
    id                  UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    challenge_id        UUID         NOT NULL,
    note_text           CLOB,
    status              VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED',
    submitted_ts        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_by        UUID         NOT NULL,
    reviewed_ts         TIMESTAMP,
    reviewed_by         UUID,
    reviewer_comment    CLOB,
    CONSTRAINT fk_submission_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id),
    CONSTRAINT fk_submission_submitted_by FOREIGN KEY (submitted_by) REFERENCES app_user(id),
    CONSTRAINT fk_submission_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES app_user(id)
);

CREATE INDEX idx_submission_submitted_by ON challenge_submission(submitted_by);
CREATE INDEX idx_submission_challenge_id ON challenge_submission(challenge_id);
CREATE INDEX idx_submission_status ON challenge_submission(status);

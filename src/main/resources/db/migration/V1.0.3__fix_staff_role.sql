-- V1.0.3 - Fix STAFF role to COACH in existing data
UPDATE app_user SET role = 'COACH' WHERE role = 'STAFF';


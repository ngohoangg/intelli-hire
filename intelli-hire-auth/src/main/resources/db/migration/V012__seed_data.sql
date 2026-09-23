-- Seed OAuth Providers
INSERT INTO oauth_providers (code, name, is_active) VALUES ('google', 'Google', TRUE);
INSERT INTO oauth_providers (code, name, is_active) VALUES ('github', 'GitHub', TRUE);

-- Seed Roles
INSERT INTO roles (name, description, is_system, is_active) VALUES ('USER', 'Standard authenticated user', TRUE, TRUE);
INSERT INTO roles (name, description, is_system, is_active) VALUES ('ADMIN', 'System administrator with elevated access', TRUE, TRUE);
INSERT INTO roles (name, description, is_system, is_active) VALUES ('SUPER_ADMIN', 'Super administrator with full system access', TRUE, TRUE);

-- Seed Permissions: User self-service
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('accounts:read:self', 'View Own Account', 'View own account information', 'user', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('accounts:update:self', 'Update Own Account', 'Update own account information', 'user', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('profiles:read:self', 'View Own Profile', 'View own user profile', 'user', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('profiles:update:self', 'Update Own Profile', 'Update own user profile', 'user', TRUE);

-- Seed Permissions: Admin account management
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:accounts:read', 'View All Accounts', 'View all user accounts', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:accounts:create', 'Create Accounts', 'Create new user accounts', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:accounts:update', 'Update Accounts', 'Update any user account', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:accounts:delete', 'Delete Accounts', 'Delete user accounts', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:accounts:lock', 'Lock/Unlock Accounts', 'Lock or unlock user accounts', 'admin', TRUE);

-- Seed Permissions: Admin role management
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:roles:read', 'View Roles', 'View all roles', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:roles:create', 'Create Roles', 'Create new roles', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:roles:update', 'Update Roles', 'Update roles', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:roles:delete', 'Delete Roles', 'Delete roles', 'admin', TRUE);
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:roles:assign', 'Assign Roles', 'Assign roles to users', 'admin', TRUE);

-- Seed Permissions: Admin permission management
INSERT INTO permissions (code, name, description, category, is_active) VALUES ('admin:permissions:read', 'View Permissions', 'View all permissions', 'admin', TRUE);

-- Assign permissions to USER role
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.name = 'USER'
  AND p.code IN ('accounts:read:self', 'accounts:update:self', 'profiles:read:self', 'profiles:update:self');

-- Assign all permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.is_active = TRUE;

-- Assign all permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.is_active = TRUE;

-- V012: Seed RBAC data (roles, permissions, role-permission assignments)

-- =============================================================================
-- 1. Insert system roles
-- =============================================================================
INSERT INTO roles (name, is_system, is_active) VALUES
    ('USER',        true, true),
    ('ADMIN',       true, true),
    ('SUPER_ADMIN', true, true);

-- =============================================================================
-- 2. Insert permissions
-- =============================================================================

-- Category: self (user's own resources)
INSERT INTO permissions (code, name, category) VALUES
    ('accounts:read:self',   'Read own account',   'self'),
    ('accounts:update:self', 'Update own account', 'self'),
    ('profiles:read:self',   'Read own profile',   'self'),
    ('profiles:update:self', 'Update own profile', 'self');

-- Category: admin:accounts
INSERT INTO permissions (code, name, category) VALUES
    ('admin:accounts:read',   'Read accounts',   'admin:accounts'),
    ('admin:accounts:create', 'Create accounts', 'admin:accounts'),
    ('admin:accounts:update', 'Update accounts', 'admin:accounts'),
    ('admin:accounts:delete', 'Delete accounts', 'admin:accounts'),
    ('admin:accounts:lock',   'Lock accounts',   'admin:accounts');

-- Category: admin:roles
INSERT INTO permissions (code, name, category) VALUES
    ('admin:roles:read',   'Read roles',   'admin:roles'),
    ('admin:roles:create', 'Create roles', 'admin:roles'),
    ('admin:roles:update', 'Update roles', 'admin:roles'),
    ('admin:roles:delete', 'Delete roles', 'admin:roles'),
    ('admin:roles:assign', 'Assign roles', 'admin:roles');

-- Category: admin:permissions
INSERT INTO permissions (code, name, category) VALUES
    ('admin:permissions:read', 'Read permissions', 'admin:permissions');

-- =============================================================================
-- 3. Assign permissions to roles
-- =============================================================================

-- USER role → 4 'self' permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
  AND p.code IN (
      'accounts:read:self',
      'accounts:update:self',
      'profiles:read:self',
      'profiles:update:self'
  );

-- ADMIN role → all 15 permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- SUPER_ADMIN role → all 15 permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN';

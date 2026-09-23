package org.aibles.intellihire.authservice.rbac.adapter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountAuthorizationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String FIND_ROLES_BY_ACCOUNT_ID = """
            SELECT r.name
            FROM roles r
            INNER JOIN account_roles ar ON r.id = ar.role_id
            WHERE ar.account_id = :accountId
              AND r.is_active = true
              AND (ar.expires_at IS NULL OR ar.expires_at > CURRENT_TIMESTAMP)
            """;

    private static final String FIND_PERMISSIONS_BY_ACCOUNT_ID = """
            SELECT DISTINCT p.code
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN roles r ON rp.role_id = r.id
            INNER JOIN account_roles ar ON r.id = ar.role_id
            WHERE ar.account_id = :accountId
              AND r.is_active = true
              AND p.is_active = true
              AND (ar.expires_at IS NULL OR ar.expires_at > CURRENT_TIMESTAMP)
            """;

    private static final String ASSIGN_DEFAULT_ROLE = """
            INSERT INTO account_roles (account_id, role_id, assigned_at, created_at)
            SELECT :accountId, r.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            FROM roles r
            WHERE r.name = 'USER' AND r.is_active = true
            ON CONFLICT (account_id, role_id) DO NOTHING
            """;

    public List<String> findRolesByAccountId(Long accountId) {
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        return jdbcTemplate.queryForList(FIND_ROLES_BY_ACCOUNT_ID, params, String.class);
    }

    public List<String> findPermissionsByAccountId(Long accountId) {
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        return jdbcTemplate.queryForList(FIND_PERMISSIONS_BY_ACCOUNT_ID, params, String.class);
    }

    public void assignDefaultRole(Long accountId) {
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        jdbcTemplate.update(ASSIGN_DEFAULT_ROLE, params);
    }
}

package org.aibles.intellihire.authservice.rbac.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionEntityTest {

    @Test
    @DisplayName("Should set and get fields for RolePermissionEntity")
    void shouldSetAndGetFields() {
        RolePermissionEntity rolePermission = new RolePermissionEntity();
        rolePermission.setId(5L);
        rolePermission.setRoleId(1L);
        rolePermission.setPermissionId(2L);

        assertThat(rolePermission.getId()).isEqualTo(5L);
        assertThat(rolePermission.getRoleId()).isEqualTo(1L);
        assertThat(rolePermission.getPermissionId()).isEqualTo(2L);
    }
}

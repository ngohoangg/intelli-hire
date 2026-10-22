package org.aibles.intellihire.authservice.rbac.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityTest {

    @Test
    @DisplayName("Should create RoleEntity with default isSystem false and isActive true")
    void shouldCreateRoleWithDefaultValues() {
        RoleEntity role = new RoleEntity();
        role.setName("CANDIDATE");

        assertThat(role.getName()).isEqualTo("CANDIDATE");
        assertThat(role.getIsSystem()).isFalse();
        assertThat(role.getIsActive()).isTrue();
        assertThat(role.isSystemRole()).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify system role")
    void shouldIdentifySystemRole() {
        RoleEntity role = new RoleEntity();
        role.setName("ADMIN");
        role.setIsSystem(true);

        assertThat(role.isSystemRole()).isTrue();

        role.setIsSystem(false);
        assertThat(role.isSystemRole()).isFalse();

        role.setIsSystem(null);
        assertThat(role.isSystemRole()).isFalse();
    }

    @Test
    @DisplayName("Should set and get all fields including audit metadata")
    void shouldSetAndGetAllFields() {
        RoleEntity role = new RoleEntity();
        LocalDateTime now = LocalDateTime.now();

        role.setId(10L);
        role.setName("RECRUITER");
        role.setIsSystem(false);
        role.setIsActive(false);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setCreatedBy(1L);
        role.setUpdatedBy(2L);

        assertThat(role.getId()).isEqualTo(10L);
        assertThat(role.getName()).isEqualTo("RECRUITER");
        assertThat(role.getIsSystem()).isFalse();
        assertThat(role.getIsActive()).isFalse();
        assertThat(role.getCreatedAt()).isEqualTo(now);
        assertThat(role.getUpdatedAt()).isEqualTo(now);
        assertThat(role.getCreatedBy()).isEqualTo(1L);
        assertThat(role.getUpdatedBy()).isEqualTo(2L);
    }
}

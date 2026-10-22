package org.aibles.intellihire.authservice.rbac.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEntityTest {

    @Test
    @DisplayName("Should set and get all fields including audit metadata")
    void shouldSetAndGetAllFields() {
        PermissionEntity permission = new PermissionEntity();
        LocalDateTime now = LocalDateTime.now();

        permission.setId(1L);
        permission.setCode("accounts:read:self");
        permission.setName("Read own account");
        permission.setCategory("self");
        permission.setCreatedAt(now);
        permission.setUpdatedAt(now);
        permission.setCreatedBy(100L);
        permission.setUpdatedBy(200L);

        assertThat(permission.getId()).isEqualTo(1L);
        assertThat(permission.getCode()).isEqualTo("accounts:read:self");
        assertThat(permission.getName()).isEqualTo("Read own account");
        assertThat(permission.getCategory()).isEqualTo("self");
        assertThat(permission.getCreatedAt()).isEqualTo(now);
        assertThat(permission.getUpdatedAt()).isEqualTo(now);
        assertThat(permission.getCreatedBy()).isEqualTo(100L);
        assertThat(permission.getUpdatedBy()).isEqualTo(200L);
    }
}

package org.aibles.intellihire.authservice.rbac.domain;

import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RbacExceptionsTest {

    @Test
    @DisplayName("RoleNotFoundException should have correct ErrorCode and details")
    void shouldCreateRoleNotFoundException() {
        RoleNotFoundException ex1 = new RoleNotFoundException();
        assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND);
        assertThat(ex1.getDetails()).isNull();

        RoleNotFoundException ex2 = new RoleNotFoundException("Role id 99 not found");
        assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND);
        assertThat(ex2.getDetails()).isEqualTo("Role id 99 not found");
    }

    @Test
    @DisplayName("PermissionNotFoundException should have correct ErrorCode and details")
    void shouldCreatePermissionNotFoundException() {
        PermissionNotFoundException ex1 = new PermissionNotFoundException();
        assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
        assertThat(ex1.getDetails()).isNull();

        PermissionNotFoundException ex2 = new PermissionNotFoundException("Permission code not found");
        assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_NOT_FOUND);
        assertThat(ex2.getDetails()).isEqualTo("Permission code not found");
    }
}

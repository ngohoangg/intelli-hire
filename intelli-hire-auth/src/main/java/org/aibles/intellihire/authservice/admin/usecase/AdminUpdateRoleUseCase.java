package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.admin.usecase.command.UpdateRoleCommand;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;

public interface AdminUpdateRoleUseCase {
    RoleEntity execute(UpdateRoleCommand command);
}

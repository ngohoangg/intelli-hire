package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.admin.usecase.command.CreateRoleCommand;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;

public interface AdminCreateRoleUseCase {
    RoleEntity execute(CreateRoleCommand command);
}

package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.admin.usecase.command.AssignRoleCommand;

public interface AdminAssignRoleUseCase {
    void execute(AssignRoleCommand command);
}

package org.aibles.intellihire.authservice.admin.usecase.command;

public record UpdateRoleCommand(
        Long roleId,
        String name,
        String description,
        Boolean isActive
) {
}

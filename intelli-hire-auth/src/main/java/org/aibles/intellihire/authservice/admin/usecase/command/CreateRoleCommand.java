package org.aibles.intellihire.authservice.admin.usecase.command;

public record CreateRoleCommand(
        String name,
        String description
) {
}

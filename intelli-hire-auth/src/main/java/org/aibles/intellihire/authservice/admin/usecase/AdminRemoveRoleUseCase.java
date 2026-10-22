package org.aibles.intellihire.authservice.admin.usecase;

public interface AdminRemoveRoleUseCase {
    void execute(Long accountId, Long roleId);
}

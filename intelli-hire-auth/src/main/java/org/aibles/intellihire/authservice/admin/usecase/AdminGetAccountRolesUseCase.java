package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;

import java.util.List;

public interface AdminGetAccountRolesUseCase {
    List<AccountRoleEntity> execute(Long accountId);
}

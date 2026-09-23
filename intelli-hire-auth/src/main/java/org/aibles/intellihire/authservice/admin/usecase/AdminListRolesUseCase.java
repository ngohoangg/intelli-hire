package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.springframework.data.domain.Page;

public interface AdminListRolesUseCase {
    Page<RoleEntity> execute(int page, int size);
}

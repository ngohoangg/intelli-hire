package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.rbac.domain.PermissionEntity;
import org.springframework.data.domain.Page;

public interface AdminListPermissionsUseCase {
    Page<PermissionEntity> execute(int page, int size);
}

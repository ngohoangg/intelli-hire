package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.rbac.adapter.repository.PermissionRepository;
import org.aibles.intellihire.authservice.rbac.domain.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminListPermissionsUseCaseImpl implements AdminListPermissionsUseCase {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionEntity> execute(int page, int size) {
        log.debug("Admin listing permissions: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "category", "code"));
        return permissionRepository.findAll(pageable);
    }
}

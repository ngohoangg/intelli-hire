package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.admin.usecase.command.CreateRoleCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.RoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCreateRoleUseCaseImpl implements AdminCreateRoleUseCase {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleEntity execute(CreateRoleCommand command) {
        log.debug("Admin creating role: {}", command.name());

        if (roleRepository.existsByName(command.name())) {
            throw new RoleNameAlreadyExistsException();
        }

        RoleEntity role = new RoleEntity();
        role.setName(command.name().toUpperCase());
        role.setDescription(command.description());
        role.setIsSystem(false);
        role.setIsActive(true);

        RoleEntity saved = roleRepository.save(role);

        log.info("Admin created role: {}", saved.getId());

        return saved;
    }

    private static class RoleNameAlreadyExistsException extends BaseException {
        RoleNameAlreadyExistsException() {
            super(ErrorCode.VALIDATION_ERROR, "Tên vai trò đã tồn tại");
        }
    }
}

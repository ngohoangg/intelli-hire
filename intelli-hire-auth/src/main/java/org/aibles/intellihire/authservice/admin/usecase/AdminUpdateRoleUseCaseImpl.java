package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.admin.usecase.command.UpdateRoleCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.RoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUpdateRoleUseCaseImpl implements AdminUpdateRoleUseCase {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleEntity execute(UpdateRoleCommand command) {
        log.debug("Admin updating role: {}", command.roleId());

        RoleEntity role = roleRepository.findById(command.roleId())
                .orElseThrow(RoleNotFoundException::new);

        if (command.name() != null) {
            role.setName(command.name().toUpperCase());
        }
        if (command.description() != null) {
            role.setDescription(command.description());
        }
        if (command.isActive() != null) {
            role.setIsActive(command.isActive());
        }

        RoleEntity saved = roleRepository.save(role);

        log.info("Admin updated role: {}", saved.getId());

        return saved;
    }
}

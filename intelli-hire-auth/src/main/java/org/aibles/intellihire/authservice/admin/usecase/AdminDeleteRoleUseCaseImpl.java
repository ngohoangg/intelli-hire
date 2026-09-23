package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.rbac.adapter.repository.RoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.CannotDeleteSystemRoleException;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeleteRoleUseCaseImpl implements AdminDeleteRoleUseCase {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void execute(Long roleId) {
        log.debug("Admin deleting role: {}", roleId);

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(RoleNotFoundException::new);

        if (role.getIsSystem()) {
            throw new CannotDeleteSystemRoleException();
        }

        roleRepository.delete(role);

        log.info("Admin deleted role: {}", roleId);
    }
}

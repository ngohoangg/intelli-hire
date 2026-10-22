package org.aibles.intellihire.authservice.profile.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public UserProfileEntity execute(UpdateProfileCommand command) {
        log.debug("Updating profile for accountId: {}", command.accountId());

        UserProfileEntity profile = userProfileRepository.findByAccountId(command.accountId())
                .orElseGet(() -> {
                    log.info("Profile not found for accountId: {}, creating lazy profile", command.accountId());
                    UserProfileEntity newProfile = new UserProfileEntity();
                    newProfile.setAccountId(command.accountId());
                    return newProfile;
                });

        // Validate unique phone if provided
        if (command.phone() != null && !command.phone().isBlank()) {
            String trimmedPhone = command.phone().trim();
            userProfileRepository.findByPhone(trimmedPhone)
                    .ifPresent(existing -> {
                        if (!existing.getAccountId().equals(command.accountId())) {
                            throw new BaseException(ErrorCode.INVALID_REQUEST, "Số điện thoại đã được sử dụng bởi tài khoản khác") {};
                        }
                    });
            profile.setPhone(trimmedPhone);
        } else if (command.phone() != null && command.phone().isBlank()) {
            profile.setPhone(null);
        }

        if (command.firstName() != null) {
            profile.setFirstName(command.firstName().trim());
        }
        if (command.middleName() != null) {
            profile.setMiddleName(command.middleName().trim());
        }
        if (command.lastName() != null) {
            profile.setLastName(command.lastName().trim());
        }
        if (command.avatarUrl() != null) {
            profile.setAvatarUrl(command.avatarUrl().trim());
        }
        if (command.address() != null) {
            profile.setAddress(command.address().trim());
        }
        if (command.birthOfDate() != null) {
            profile.setBirthOfDate(command.birthOfDate());
        }
        if (command.gender() != null) {
            profile.setGender(command.gender().trim());
        }
        if (command.githubUrl() != null) {
            profile.setGithubUrl(command.githubUrl().trim());
        }
        if (command.linkedInUrl() != null) {
            profile.setLinkedInUrl(command.linkedInUrl().trim());
        }

        UserProfileEntity saved = userProfileRepository.save(profile);
        log.info("Profile updated successfully for accountId: {}", command.accountId());
        return saved;
    }
}

package org.aibles.intellihire.authservice.profile.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.ProfileNotFoundException;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;
import org.aibles.intellihire.authservice.shared.util.InputSanitizer;
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
        log.debug("Updating profile for account: {}", command.accountId());

        UserProfileEntity profile = userProfileRepository.findByAccountId(command.accountId())
                .orElseThrow(ProfileNotFoundException::new);

        // Update fields if provided, sanitizing text inputs
        if (command.firstName() != null) {
            profile.setFirstName(InputSanitizer.sanitizeText(command.firstName()));
        }
        if (command.middleName() != null) {
            profile.setMiddleName(InputSanitizer.sanitizeText(command.middleName()));
        }
        if (command.lastName() != null) {
            profile.setLastName(InputSanitizer.sanitizeText(command.lastName()));
        }
        if (command.avatarUrl() != null) {
            profile.setAvatarUrl(InputSanitizer.sanitizeUrl(command.avatarUrl()));
        }
        if (command.phone() != null) {
            profile.setPhone(InputSanitizer.sanitizeText(command.phone()));
        }
        if (command.address() != null) {
            profile.setAddress(InputSanitizer.sanitizeText(command.address()));
        }
        if (command.birthOfDate() != null) {
            profile.setBirthOfDate(command.birthOfDate());
        }
        if (command.gender() != null) {
            profile.setGender(InputSanitizer.sanitizeText(command.gender()));
        }
        if (command.githubUrl() != null) {
            profile.setGithubUrl(InputSanitizer.sanitizeUrl(command.githubUrl()));
        }
        if (command.linkedInUrl() != null) {
            profile.setLinkedInUrl(InputSanitizer.sanitizeUrl(command.linkedInUrl()));
        }

        UserProfileEntity saved = userProfileRepository.save(profile);
        log.info("Profile updated for account: {}", command.accountId());

        return saved;
    }
}

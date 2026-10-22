package org.aibles.intellihire.authservice.profile.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.ProfileNotFoundException;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProfileUseCaseImpl implements GetProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileEntity execute(Long accountId) {
        log.debug("Fetching profile for accountId: {}", accountId);
        return userProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));
    }
}

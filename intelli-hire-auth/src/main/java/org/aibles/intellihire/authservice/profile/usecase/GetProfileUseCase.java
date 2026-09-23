package org.aibles.intellihire.authservice.profile.usecase;

import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;

public interface GetProfileUseCase {
    UserProfileEntity execute(Long accountId);
}

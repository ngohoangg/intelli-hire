package org.aibles.intellihire.authservice.profile.usecase;

import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;

public interface UpdateProfileUseCase {

    UserProfileEntity execute(UpdateProfileCommand command);
}

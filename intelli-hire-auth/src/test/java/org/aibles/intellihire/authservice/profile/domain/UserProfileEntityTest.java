package org.aibles.intellihire.authservice.profile.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileEntityTest {

    @Test
    @DisplayName("Should build and format full name correctly with all components")
    void shouldFormatFullNameWithAllParts() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("Nguyen");
        profile.setMiddleName("Van");
        profile.setLastName("An");

        assertThat(profile.getFullName()).isEqualTo("Nguyen Van An");
    }

    @Test
    @DisplayName("Should format full name when middle name is null")
    void shouldFormatFullNameWithoutMiddleName() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("John");
        profile.setLastName("Doe");

        assertThat(profile.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should set and get profile fields correctly")
    void shouldSetAndGetProfileFields() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(1L);
        profile.setAccountId(100L);
        profile.setPhone("0987654321");
        profile.setAddress("Hanoi, Vietnam");
        profile.setGender("MALE");
        profile.setBirthOfDate(LocalDate.of(2000, 1, 15));
        profile.setAvatarUrl("https://example.com/avatar.png");
        profile.setGithubUrl("https://github.com/test");
        profile.setLinkedInUrl("https://linkedin.com/in/test");

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getAccountId()).isEqualTo(100L);
        assertThat(profile.getPhone()).isEqualTo("0987654321");
        assertThat(profile.getAddress()).isEqualTo("Hanoi, Vietnam");
        assertThat(profile.getGender()).isEqualTo("MALE");
        assertThat(profile.getBirthOfDate()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(profile.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(profile.getGithubUrl()).isEqualTo("https://github.com/test");
        assertThat(profile.getLinkedInUrl()).isEqualTo("https://linkedin.com/in/test");
    }
}

package org.aibles.intellihire.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAccountControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private AccountRepository accountRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Find or create an admin account and generate a token for it
        // The seed data should have admin account; generate a token with ADMIN role
        AccountEntity admin = accountRepository.findByEmail("admin@intellihire.com")
                .orElseGet(() -> {
                    AccountEntity a = new AccountEntity();
                    a.setEmail("admin@intellihire.com");
                    a.setUsername("admin");
                    a.setPassword("$2a$12$dummy"); // not used for auth in tests
                    a.setStatus(org.aibles.intellihire.authservice.account.domain.AccountStatus.ACTIVE);
                    return accountRepository.save(a);
                });

        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                admin.getId(), admin.getEmail(), admin.getUsername(),
                List.of("ADMIN"), List.of("MANAGE_ACCOUNTS", "MANAGE_ROLES")
        );
        adminToken = bundle.accessToken();
    }

    @Test
    void listAccounts_withAdminToken_success() throws Exception {
        mockMvc.perform(get("/v1/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listAccounts_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/admin/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAccounts_withNonAdminToken_forbidden() throws Exception {
        JwtTokenBundle userBundle = jwtTokenService.issueTokenPair(
                999L, "user@test.com", "testuser",
                List.of("USER"), List.of()
        );

        mockMvc.perform(get("/v1/admin/accounts")
                        .header("Authorization", "Bearer " + userBundle.accessToken()))
                .andExpect(status().isForbidden());
    }
}

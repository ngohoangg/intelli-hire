package org.aibles.intellihire.authservice.admin.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.admin.adapter.web.dto.PermissionResponseDTO;
import org.aibles.intellihire.authservice.admin.usecase.AdminListPermissionsUseCase;
import org.aibles.intellihire.authservice.rbac.domain.PermissionEntity;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Admin - Permissions", description = "Admin permission management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminPermissionController {

    private final AdminListPermissionsUseCase adminListPermissionsUseCase;

    @GetMapping
    @Operation(summary = "List permissions", description = "Returns a paginated list of all permissions")
    public ResponseEntity<ApiResponse<Page<PermissionResponseDTO>>> listPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        log.debug("Admin listing permissions: page={}, size={}", page, size);

        Page<PermissionEntity> permissions = adminListPermissionsUseCase.execute(page, size);

        return ResponseEntity.ok(ApiResponse.success(permissions.map(this::toPermissionResponseDTO)));
    }

    private PermissionResponseDTO toPermissionResponseDTO(PermissionEntity permission) {
        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .description(permission.getDescription())
                .category(permission.getCategory())
                .isActive(permission.getIsActive())
                .build();
    }
}

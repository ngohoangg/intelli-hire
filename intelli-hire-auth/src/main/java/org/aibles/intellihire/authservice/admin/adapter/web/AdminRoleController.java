package org.aibles.intellihire.authservice.admin.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.admin.adapter.web.dto.*;
import org.aibles.intellihire.authservice.admin.usecase.*;
import org.aibles.intellihire.authservice.admin.usecase.command.*;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin - Roles", description = "Admin role management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final AdminListRolesUseCase adminListRolesUseCase;
    private final AdminCreateRoleUseCase adminCreateRoleUseCase;
    private final AdminUpdateRoleUseCase adminUpdateRoleUseCase;
    private final AdminDeleteRoleUseCase adminDeleteRoleUseCase;

    @GetMapping
    @Operation(summary = "List roles", description = "Returns a paginated list of all roles")
    public ResponseEntity<ApiResponse<Page<RoleResponseDTO>>> listRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("Admin listing roles: page={}, size={}", page, size);

        Page<RoleEntity> roles = adminListRolesUseCase.execute(page, size);

        return ResponseEntity.ok(ApiResponse.success(roles.map(this::toRoleResponseDTO)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create role", description = "Creates a new role")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> createRole(
            @Valid @RequestBody CreateRoleRequestDTO requestDTO
    ) {
        log.info("Admin creating role: {}", requestDTO.getName());

        CreateRoleCommand command = new CreateRoleCommand(
                requestDTO.getName(),
                requestDTO.getDescription()
        );

        RoleEntity role = adminCreateRoleUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toRoleResponseDTO(role)));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update role", description = "Updates an existing role")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequestDTO requestDTO
    ) {
        log.info("Admin updating role: {}", roleId);

        UpdateRoleCommand command = new UpdateRoleCommand(
                roleId,
                requestDTO.getName(),
                requestDTO.getDescription(),
                requestDTO.getIsActive()
        );

        RoleEntity role = adminUpdateRoleUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toRoleResponseDTO(role)));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete role", description = "Deletes a role (only non-system roles can be deleted)")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        log.info("Admin deleting role: {}", roleId);

        adminDeleteRoleUseCase.execute(roleId);

        return ResponseEntity.noContent().build();
    }

    private RoleResponseDTO toRoleResponseDTO(RoleEntity role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .build();
    }
}

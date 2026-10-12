package com.example.ztna_server.api;

import com.example.ztna_server.api.dto.AssignSystemRoleRequest;
import com.example.ztna_server.api.dto.SystemRoleRequest;
import com.example.ztna_server.api.dto.SystemRoleResponse;
import com.example.ztna_server.api.dto.UserSummaryResponse;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.exception.AppException;
import com.example.ztna_server.exception.ErrorCode;
import com.example.ztna_server.service.SystemRoleService;
import com.example.ztna_server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-roles")
@RequiredArgsConstructor
public class SystemRoleController {

    private final SystemRoleService systemRoleService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemRoleResponse> create(@RequestBody SystemRoleRequest request) {
        Actor actor = getCurrentActor();

        SystemRoleResponse response = systemRoleService.create(
                request, actor.id(), actor.username());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemRoleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(systemRoleService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SystemRoleResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(systemRoleService.getAll(pageable));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemRoleResponse> update(
            @PathVariable Long id,
            @RequestBody SystemRoleRequest request) {

        Actor actor = getCurrentActor();

        SystemRoleResponse response = systemRoleService.update(
                id, request, actor.id(), actor.username());

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Actor actor = getCurrentActor();

        systemRoleService.delete(id, actor.id(), actor.username());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSummaryResponse> assignRole(
            @RequestBody AssignSystemRoleRequest request) {

        Actor actor = getCurrentActor();

        UserSummaryResponse response = systemRoleService.assignRole(
                request, actor.id(), actor.username());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getUsersByRole(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(systemRoleService.getUsersByRole(id, pageable));
    }

    private Actor getCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // hoặc lỗi phù hợp của bạn
        }

        String username = auth.getName();

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return new Actor(user.getId(), user.getUsername());
    }

    private record Actor(Long id, String username) {}
}
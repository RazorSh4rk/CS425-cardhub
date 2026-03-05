package com.cardhub.controller;

import com.cardhub.dto.ChangeRoleRequest;
import com.cardhub.dto.UserResponse;
import com.cardhub.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserResponse::from).toList());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id,
                                                    @Valid @RequestBody ChangeRoleRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(request.role());
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }

    @PatchMapping("/users/{id}/toggle")
    public ResponseEntity<UserResponse> toggleAccount(@PathVariable Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountEnabled(!user.isAccountEnabled());
        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

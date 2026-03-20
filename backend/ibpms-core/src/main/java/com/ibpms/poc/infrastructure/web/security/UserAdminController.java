package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.security.PasswordResetResponseDTO;
import com.ibpms.poc.application.dto.security.UserCreateRequestDTO;
import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.dto.security.UserUpdateRequestDTO;
import com.ibpms.poc.application.service.security.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.ibpms.poc.infrastructure.jpa.repository.security.TokenBlacklistRepository;
import com.ibpms.poc.infrastructure.jpa.entity.security.TokenBlacklistEntity;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;
    private final TokenBlacklistRepository blacklistRepository;

    public UserAdminController(UserService userService, TokenBlacklistRepository blacklistRepository) {
        this.userService = userService;
        this.blacklistRepository = blacklistRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.listAll());
    }

    // CA-3: Reset Manual Temp Pass
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<PasswordResetResponseDTO> resetPassword(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.resetPassword(id));
    }

    // CA-5: Deactivate (Kill Switch explícito UI)
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    // US-036 p2: Soft-Delete Guard Enforced
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID id) {
        // Rechazo físico explícito: 405 Method Not Allowed
        Map<String, String> response = new HashMap<>();
        response.put("error", "Method Not Allowed");
        response.put("message", "El borrado físico de identidades está prohibido. Utilice el Soft-Delete (/deactivate).");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // CA-14: Exorcismo JWT (Kill Session Extremo)
    @PostMapping("/{id}/kill-session")
    public ResponseEntity<Map<String, String>> killSession(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // En un caso real, la firma vendría del cliente o de un gestor de tokens activos.
        // Aquí demostramos la inserción de un token a la lista mediante el header interceptado o paramétrico.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                TokenBlacklistEntity blackToken = new TokenBlacklistEntity(hexString.toString(), LocalDateTime.now().plusDays(1), null);
                blacklistRepository.save(blackToken);
            } catch (NoSuchAlgorithmException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        
        // También cerramos la transaccionalidad desactivando al usuario base transitoriamente
        userService.deactivateUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "JWT purgado enviando la firma a Sandbox (Lista Negra). Contexto liquidado.");
        return ResponseEntity.ok(response);
    }
}

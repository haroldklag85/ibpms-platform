package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.security.DelegationRequestDTO;
import com.ibpms.poc.application.dto.security.PasswordResetResponseDTO;
import com.ibpms.poc.application.dto.security.UserCreateRequestDTO;
import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.dto.security.UserUpdateRequestDTO;
import com.ibpms.poc.application.service.security.DelegationService;
import com.ibpms.poc.application.service.security.UserService;
import com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity;
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
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;
    private final TokenBlacklistRepository blacklistRepository;
    private final DelegationService delegationService;

    public UserAdminController(UserService userService,
                               TokenBlacklistRepository blacklistRepository,
                               DelegationService delegationService) {
        this.userService = userService;
        this.blacklistRepository = blacklistRepository;
        this.delegationService = delegationService;
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

    // @Traceability: US-036 - CA-02 El Guardián Absoluto (Prohibición Global de Delete Físico)
    // US-036 p2: Soft-Delete Guard Enforced
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID id) {
        // Rechazo físico explícito: 405 Method Not Allowed
        Map<String, String> response = new HashMap<>();
        response.put("error", "Method Not Allowed");
        response.put("message", "El borrado físico de identidades está prohibido. Utilice el Soft-Delete (/deactivate).");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * CA-9 US-036 — Cesión Temporal de Poder (Delegación Autónoma Timebox).
     * POST /api/v1/admin/users/{id}/delegate
     * Body: { recipientId, startDate, endDate, reason }
     * Respuesta 201: entidad DelegationEntity persistida.
     *
     * El JwtAuthFilter consume DelegationRepository en tiempo real para inyectar
     * los roles del donante en el contexto JWT del receptor durante la ventana activa.
     */
    @PostMapping("/{id}/delegate")
    public ResponseEntity<?> delegate(
            @PathVariable UUID id,
            @RequestBody DelegationRequestDTO dto) {
        
        String currentUserEmail = com.ibpms.poc.application.util.SecurityContextUtils.getAssignee();
        boolean isSuperAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity donor = userService.findById(id).orElse(null);
        if (donor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Donante no encontrado"));
        }

        if (!isSuperAdmin && !donor.getEmail().equals(currentUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "IDOR detectado: No tienes permisos para delegar los permisos de este usuario."));
        }

        DelegationEntity created = delegationService.createDelegation(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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

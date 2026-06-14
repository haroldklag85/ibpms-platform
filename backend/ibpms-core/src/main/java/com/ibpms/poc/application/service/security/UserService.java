package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.dto.security.PasswordResetResponseDTO;
import com.ibpms.poc.application.dto.security.UserCreateRequestDTO;
import com.ibpms.poc.application.dto.security.UserResponseDTO;
import com.ibpms.poc.application.dto.security.UserUpdateRequestDTO;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.mq.producer.TaskRescueProducer;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ibpms.poc.application.service.ui.MenuLayoutService;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskRescueProducer taskRescueProducer;
    private final MenuLayoutService menuLayoutService;
    private final com.ibpms.poc.application.port.out.AuditLogPort auditLogPort;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, TaskRescueProducer taskRescueProducer, MenuLayoutService menuLayoutService, com.ibpms.poc.application.port.out.AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRescueProducer = taskRescueProducer;
        this.menuLayoutService = menuLayoutService;
        this.auditLogPort = auditLogPort;
    }

    public UserResponseDTO createUser(UserCreateRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El username ya se encuentra en uso.");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya se encuentra registrado.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setIsExternalIdp(dto.getIsExternalIdp());
        user.setStatus(UserStatus.ACTIVE);

        if (!dto.getIsExternalIdp()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(dto.getRoleIds())));
        }

        userRepository.save(user);
        return toDto(user);
    }

    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO dto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if ("[Super_Administrador]".equals(user.getUsername())) {
             throw new org.springframework.security.access.AccessDeniedException("Imposible modificar al Super Administrador Root.");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getIsActive() != null) {
            user.setStatus(Boolean.TRUE.equals(dto.getIsActive()) ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        }
        if (dto.getStatus() != null) {
            user.setStatus(UserStatus.valueOf(dto.getStatus()));
        }
        if (dto.getIsExternalIdp() != null) {
            user.setIsExternalIdp(dto.getIsExternalIdp());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank() && !user.getIsExternalIdp()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRoleIds() != null) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(dto.getRoleIds())));
        }

        userRepository.save(user);

        // CA-17: Traza Indeleble de Auditoría
        String adminUser = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                           org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
        
        String details = String.format("{\"email\":\"%s\",\"status\":\"%s\",\"roles\":[%s]}",
            user.getEmail(), user.getStatus(), user.getRoles().stream().map(r -> "\"" + r.getName() + "\"").collect(Collectors.joining(",")));

        auditLogPort.saveAuditLog(
            UUID.randomUUID().toString(),
            "USER",
            user.getId().toString(),
            "UPDATE_USER",
            adminUser,
            java.time.LocalDateTime.now(),
            null,
            false,
            false,
            details
        );

        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            // CA-08 Trigger mass unclaim if user was deactivated
            taskRescueProducer.triggerDeactivationUnclaim(id.toString());
        }

        // CA-32: Auto-curación del menú cacheado cuando los roles del usuario cambian
        menuLayoutService.invalidateMenuTopology(user.getUsername());

        return toDto(user);
    }

    // CA-3: Reset Manual de Contraseña
    public PasswordResetResponseDTO resetPassword(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (user.getIsExternalIdp()) {
            throw new IllegalArgumentException("No se puede resetear contraseña de un usuario gestionado por IDP externo.");
        }

        String tempPassword = generateComplexRandomPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        return new PasswordResetResponseDTO(tempPassword, "La contraseña temporal se ha generado con éxito. Cópiela ahora; no podrá visualizarse nuevamente.");
    }

    // CA-5: Kill-Switch Explícito
    public void deactivateUser(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if ("[Super_Administrador]".equals(user.getUsername())) {
             throw new org.springframework.security.access.AccessDeniedException("El usuario Root no puede ser desactivado (Inmutabilidad).");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        
        // CA-17: Traza Indeleble
        String adminUser = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                           org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
        auditLogPort.saveAuditLog(
            UUID.randomUUID().toString(),
            "USER",
            user.getId().toString(),
            "DEACTIVATE_USER",
            adminUser,
            java.time.LocalDateTime.now(),
            null,
            false,
            false,
            "{\"status\":\"INACTIVE\"}"
        );

        // CA-08 Trigger mass unclaim since user is deactivated
        taskRescueProducer.triggerDeactivationUnclaim(id.toString());
        
        // CA-32: Auto-curación del menú cacheado
        menuLayoutService.invalidateMenuTopology(user.getUsername());
        
        // Nota Arquitectura: el rechazo final de los JWT activos de este usuario lo hace el JwtAuthFilter en vivo contra JPA.
    }

    /**
     * CA-07 US-036: Soft-Delete.
     * Prohibido el borrado físico de registros de usuario por trazabilidad.
     */
    public void deleteUser(UUID id) {
        deactivateUser(id);
    }

    public List<UserResponseDTO> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public java.util.Optional<UserEntity> findById(UUID id) {
        return userRepository.findById(id);
    }

    // @Traceability: US-027 - CA-04 (ADR-001 Refactor)
    // @Traceability: US-000 BUG-FIX Resilencia contra duplicidad de DataSeeder
    public java.util.Optional<UserEntity> findByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

    // @Traceability: US-027 - CA-04 (ADR-001 Refactor)
    public java.util.Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // @Traceability: US-027 - CA-04 (ADR-001 Refactor)
    public java.util.Optional<Boolean> isUserActive(String username) {
        return userRepository.isUserActive(username);
    }

    // @Traceability: US-027 - CA-04 (ADR-001 Refactor)
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    private String generateComplexRandomPassword() {
        // Garantiza: Al menos 8 chars, 1 Mayúscula, 1 número, 1 símbolo.
        String uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowers = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "@$!%*?&";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();
        sb.append(uppers.charAt(random.nextInt(uppers.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(symbols.charAt(random.nextInt(symbols.length())));

        String all = uppers + lowers + digits + symbols;
        for (int i = 0; i < 9; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle
        char[] array = sb.toString().toCharArray();
        for (int i = 0; i < array.length; i++) {
            int r = random.nextInt(array.length);
            char temp = array[i];
            array[i] = array[r];
            array[r] = temp;
        }

        return new String(array);
    }

    private UserResponseDTO toDto(UserEntity entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setIsActive(UserStatus.ACTIVE.equals(entity.getStatus()));
        dto.setStatus(entity.getStatus().name());
        dto.setIsExternalIdp(entity.getIsExternalIdp());
        
        if (entity.getRoles() != null) {
            dto.setRoles(entity.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toList()));
        }
        return dto;
    }
}

package com.ibpms.poc.application.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import java.util.UUID;

public class UserCreateRequestDTO {

    @NotBlank(message = "El username es obligatorio.")
    private String username;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "Formato de correo inválido.")
    private String email;

    // CA-2: Política de Contraseñas Fuerte
    @NotBlank(message = "La contraseña es obligatoria en la creación.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "La contraseña debe tener mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.")
    private String password;

    private Boolean isExternalIdp = false;

    private Set<UUID> roleIds;

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getIsExternalIdp() { return isExternalIdp; }
    public void setIsExternalIdp(Boolean isExternalIdp) { this.isExternalIdp = isExternalIdp; }
    public Set<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<UUID> roleIds) { this.roleIds = roleIds; }
}

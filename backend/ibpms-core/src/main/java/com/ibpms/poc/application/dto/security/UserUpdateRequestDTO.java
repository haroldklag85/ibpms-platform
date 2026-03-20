package com.ibpms.poc.application.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import java.util.UUID;

public class UserUpdateRequestDTO {

    @Email(message = "Formato de correo inválido.")
    private String email;

    // Opcional en update, pero si se envía debe cumplir
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "La contraseña debe tener mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.")
    private String password;

    private Boolean isActive;
    
    private Boolean isExternalIdp;

    private Set<UUID> roleIds;

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsExternalIdp() { return isExternalIdp; }
    public void setIsExternalIdp(Boolean isExternalIdp) { this.isExternalIdp = isExternalIdp; }
    public Set<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<UUID> roleIds) { this.roleIds = roleIds; }
}

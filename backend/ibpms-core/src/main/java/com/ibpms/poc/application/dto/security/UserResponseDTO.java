package com.ibpms.poc.application.dto.security;

import java.util.List;
import java.util.UUID;

public class UserResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isExternalIdp;
    private List<String> roles; // Solo retornamos los nombres de los roles por seguridad / simplicidad

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsExternalIdp() { return isExternalIdp; }
    public List<String> getRoles() { return roles; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setIsExternalIdp(Boolean isExternalIdp) { this.isExternalIdp = isExternalIdp; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

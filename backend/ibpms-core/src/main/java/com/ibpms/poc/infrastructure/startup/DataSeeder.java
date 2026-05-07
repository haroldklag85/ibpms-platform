package com.ibpms.poc.infrastructure.startup;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // @Traceability: US-036 - CA-02 El Guardián Absoluto (Día Cero Bootstrap)
        // CA-2: Forzar inyección del Super_Administrador si no existe
        RoleEntity rootRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_SUPER_ADMIN", "Permisos Totales Root")));

        if (userRepository.findByUsername("[Super_Administrador]").isEmpty()) {
            UserEntity rootUser = new UserEntity();
            rootUser.setUsername("[Super_Administrador]");
            rootUser.setEmail("root@ibpms.local");
            rootUser.setStatus(UserStatus.ACTIVE);
            rootUser.setIsExternalIdp(false);
            
            // Password hardcodeado temporalmente en CA-2 para bootstrap 
            // Esto luego se rotará o leerá de Environment Var
            rootUser.setPasswordHash(passwordEncoder.encode("Root#Temp4Sys"));
            
            rootUser.getRoles().add(rootRole);
            userRepository.save(rootUser);
            log.info("====== ROOT ADMIN SEED COMPLETED ======");
        }

        // CA-08: Seed ROLE_USER_INTERNAL
        if (roleRepository.findByName("ROLE_USER_INTERNAL").isEmpty()) {
            roleRepository.save(new RoleEntity("ROLE_USER_INTERNAL", "Ciudadano Interno - SSO Default"));
            log.info("====== ROLE_USER_INTERNAL SEED COMPLETED ======");
        }
    }
}

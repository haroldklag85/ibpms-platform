package com.ibpms.poc.infrastructure.startup;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
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
        // CA-2: Forzar inyección del Super_Administrador si no existe
        RoleEntity rootRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_SUPER_ADMIN", "Permisos Totales Root")));

        if (userRepository.findByUsername("[Super_Administrador]").isEmpty()) {
            UserEntity rootUser = new UserEntity();
            rootUser.setUsername("[Super_Administrador]");
            rootUser.setEmail("root@ibpms.local");
            rootUser.setIsActive(true);
            rootUser.setIsExternalIdp(false);
            
            // Password hardcodeado temporalmente en CA-2 para bootstrap 
            // Esto luego se rotará o leerá de Environment Var
            rootUser.setPasswordHash(passwordEncoder.encode("Root#Temp4Sys"));
            
            rootUser.getRoles().add(rootRole);
            userRepository.save(rootUser);
            System.out.println("====== ROOT ADMIN SEED COMPLETED ======");
        }
    }
}

package com.ibpms.poc;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenTest {
    @Test
    public void generatePassword() {
        System.out.println("HASH_IS:" + new BCryptPasswordEncoder().encode("admin123"));
    }
}

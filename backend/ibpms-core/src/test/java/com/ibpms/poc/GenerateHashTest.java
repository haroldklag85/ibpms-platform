package com.ibpms.poc;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHashTest {
    @Test
    public void generateHash() {
        System.out.println("HASH_START|" + new BCryptPasswordEncoder(10).encode("Test123!") + "|HASH_END");
    }
}

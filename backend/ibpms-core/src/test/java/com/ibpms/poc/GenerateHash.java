package com.ibpms.poc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        System.out.println("HASH_START|" + new BCryptPasswordEncoder(10).encode("Test123!") + "|HASH_END");
    }
}

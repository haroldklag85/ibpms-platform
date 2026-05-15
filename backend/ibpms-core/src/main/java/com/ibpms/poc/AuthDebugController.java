package com.ibpms.poc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;
@RestController
public class AuthDebugController {
    @GetMapping("/api/v1/debug/auth")
    public String getAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "No Authentication";
        return auth.getName() + " -> " + auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.joining(","));
    }
}

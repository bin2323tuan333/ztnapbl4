package com.example.ztna_server.security;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return UUID.fromString(auth.getPrincipal().toString());
    }
}

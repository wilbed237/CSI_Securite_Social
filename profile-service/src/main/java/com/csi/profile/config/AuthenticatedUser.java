package com.csi.profile.config;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String username, String email, List<String> roles) {
    public String primaryRole() {
        return roles == null || roles.isEmpty() ? "UNKNOWN" : roles.get(0);
    }
}

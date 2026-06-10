package com.csi.reimbursement.config;

import java.util.UUID;
import java.util.List;

public record AuthenticatedUser(UUID userId, String username, List<String> roles) {}

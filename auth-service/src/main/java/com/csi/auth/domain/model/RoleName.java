package com.csi.auth.domain.model;

/**
 * Roles portes par un compte utilisateur et injectes dans le JWT.
 */
public enum RoleName {
    AGENT,
    SOCIAL_AGENT,
    AGENT_SOCIAL,
    SECURITY_AGENT,
    DOCTOR,
    GENERALIST,
    SPECIALIST,
    ADMIN
}

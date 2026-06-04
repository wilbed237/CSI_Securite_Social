package com.csi.auth.domain.model;

/**
 * Roles portes par un compte utilisateur et injectes dans le JWT.
 */
public enum RoleName {
    AGENT,
    DOCTOR,
    GENERALIST,
    SPECIALIST,
    ADMIN
}

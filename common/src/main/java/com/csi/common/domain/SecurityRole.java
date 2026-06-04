package com.csi.common.domain;

/**
 * Roles applicatifs partages par les microservices pour proteger les endpoints.
 */
public enum SecurityRole {
    AGENT,
    DOCTOR,
    GENERALIST,
    SPECIALIST,
    ADMIN
}

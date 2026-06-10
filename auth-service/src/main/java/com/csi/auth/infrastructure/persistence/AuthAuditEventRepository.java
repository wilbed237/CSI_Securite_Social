package com.csi.auth.infrastructure.persistence;

import com.csi.auth.domain.model.AuthAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, UUID> {}

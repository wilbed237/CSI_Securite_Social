package com.csi.medical.infrastructure.persistence;
import com.csi.medical.domain.model.MedicalAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface MedicalAuditEventRepository extends JpaRepository<MedicalAuditEvent, UUID> {}

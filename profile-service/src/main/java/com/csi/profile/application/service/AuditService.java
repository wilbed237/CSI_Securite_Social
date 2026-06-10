package com.csi.profile.application.service;

import com.csi.profile.application.dto.ProfileDtos.AuditEventResponse;
import com.csi.profile.config.AuthenticatedUser;
import com.csi.profile.domain.model.AuditEvent;
import com.csi.profile.infrastructure.persistence.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditEventRepository repository;

    public void record(AuthenticatedUser actor, String action, String resourceType, String resourceId, String metadata) {
        repository.save(AuditEvent.builder().actorUserId(actor == null ? null : actor.userId())
                .actorRole(actor == null ? "SYSTEM" : actor.primaryRole()).action(action).resourceType(resourceType)
                .resourceId(resourceId).result("SUCCESS").metadata(metadata).build());
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> list(Pageable pageable) {
        return repository.findAllByOrderByTimestampDesc(pageable).map(event -> new AuditEventResponse(event.getId(), event.getActorUserId(), event.getActorRole(), event.getAction(), event.getResourceType(), event.getResourceId(), event.getTimestamp(), event.getResult(), event.getMetadata()));
    }
}

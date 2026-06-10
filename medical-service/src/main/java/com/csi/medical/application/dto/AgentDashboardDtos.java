package com.csi.medical.application.dto;

import com.csi.medical.domain.model.DoctorType;
import java.util.List;

public final class AgentDashboardDtos {
    private AgentDashboardDtos() {}
    public record DoctorConsultedStatsResponse(long totalConsultations, long generalistConsultations, long specialistConsultations, List<DoctorTypeCountResponse> byType) {}
    public record DoctorTypeCountResponse(DoctorType type, long count) {}
}

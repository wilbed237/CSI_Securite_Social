package com.csi.medical.application.service;

import com.csi.medical.application.dto.AgentDashboardDtos.*;
import com.csi.medical.domain.model.DoctorType;
import com.csi.medical.infrastructure.persistence.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentDashboardService {
    private final ConsultationRepository consultations;

    @Transactional(readOnly = true)
    public DoctorConsultedStatsResponse doctorsConsulted(String period, LocalDate startDate, LocalDate endDate) {
        DateRange range = range(period, startDate, endDate);
        long generalists = consultations.countByDoctorTypeAndStartedAtBetween(DoctorType.GENERALIST, range.start(), range.end());
        long specialists = consultations.countByDoctorTypeAndStartedAtBetween(DoctorType.SPECIALIST, range.start(), range.end());
        return new DoctorConsultedStatsResponse(generalists + specialists, generalists, specialists,
                List.of(new DoctorTypeCountResponse(DoctorType.GENERALIST, generalists), new DoctorTypeCountResponse(DoctorType.SPECIALIST, specialists)));
    }

    private DateRange range(String requested, LocalDate customStart, LocalDate customEnd) {
        String period = requested == null ? "MONTH" : requested;
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Douala"));
        LocalDate start = switch (period) {
            case "DAY" -> today;
            case "WEEK" -> today.minusDays(today.getDayOfWeek().getValue() - 1L);
            case "QUARTER" -> today.minusMonths(2).withDayOfMonth(1);
            case "YEAR" -> today.withDayOfYear(1);
            case "CUSTOM" -> customStart == null ? today : customStart;
            default -> today.withDayOfMonth(1);
        };
        LocalDate end = "CUSTOM".equals(period) && customEnd != null ? customEnd : today;
        return new DateRange(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }
    private record DateRange(LocalDateTime start, LocalDateTime end) {}
}

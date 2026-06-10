package com.csi.profile.application.service;

import com.csi.profile.application.dto.DashboardDtos.*;
import com.csi.profile.application.dto.DashboardPeriod;
import com.csi.profile.application.mapper.ProfileMapper;
import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.domain.model.InsuredStatus;
import com.csi.profile.infrastructure.persistence.DoctorRepository;
import com.csi.profile.infrastructure.persistence.InsuredPersonRepository;
import com.csi.profile.infrastructure.persistence.SocialAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agregations locales du dashboard agent social.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final InsuredPersonRepository insuredRepository;
    private final DoctorRepository doctorRepository;
    private final SocialAgentRepository socialAgentRepository;
    private final ProfileMapper mapper;

    @Transactional(readOnly = true)
    public AgentSocialSummaryResponse agentSocialSummary(DashboardPeriod period, LocalDate startDate, LocalDate endDate,
                                                          DoctorType doctorType, String specialty) {
        InstantRange range = resolveRange(period, startDate, endDate);
        Specification<com.csi.profile.domain.model.Doctor> doctorSpec = doctorSpecification(doctorType, specialty);
        long filteredDoctors = doctorRepository.count(doctorSpec);
        long filteredActiveDoctors = doctorRepository.count(doctorSpec.and((root, query, cb) -> cb.isTrue(root.get("active"))));
        return new AgentSocialSummaryResponse(
                insuredRepository.count(),
                insuredRepository.countByStatus(InsuredStatus.ACTIVE),
                insuredRepository.countByStatus(InsuredStatus.SUSPENDED),
                insuredRepository.countByCreatedAtBetween(range.start(), range.end()),
                insuredRepository.countByCreatedAtGreaterThanEqual(startOfCurrentMonth()),
                filteredDoctors,
                filteredActiveDoctors,
                filteredDoctors - filteredActiveDoctors,
                doctorRepository.countByType(DoctorType.GENERALIST),
                doctorRepository.countByType(DoctorType.SPECIALIST),
                socialAgentRepository.count(),
                socialAgentRepository.countByActiveTrue(),
                insuredMonthlyEvolution(),
                doctorsByType());
    }

    @Transactional(readOnly = true)
    public PatientsStatsResponse patientsStats(DashboardPeriod period, LocalDate startDate, LocalDate endDate) {
        InstantRange range = resolveRange(period, startDate, endDate);
        return new PatientsStatsResponse(
                insuredRepository.count(),
                insuredRepository.countByStatus(InsuredStatus.ACTIVE),
                insuredRepository.countByStatus(InsuredStatus.SUSPENDED),
                insuredRepository.countByCreatedAtBetween(range.start(), range.end()),
                insuredRepository.countByCreatedAtGreaterThanEqual(startOfCurrentMonth()),
                insuredMonthlyEvolution());
    }

    @Transactional(readOnly = true)
    public DoctorsStatsResponse doctorsStats() {
        long total = doctorRepository.count();
        long active = doctorRepository.countByActiveTrue();
        return new DoctorsStatsResponse(
                total,
                active,
                total - active,
                doctorRepository.countByType(DoctorType.GENERALIST),
                doctorRepository.countByType(DoctorType.SPECIALIST),
                doctorsByType());
    }

    @Transactional(readOnly = true)
    public RecentActivitiesResponse recentActivities() {
        return new RecentActivitiesResponse(
                insuredRepository.findTop5ByOrderByCreatedAtDesc().stream().map(mapper::toInsuredResponse).toList(),
                doctorRepository.findTop5ByOrderByCreatedAtDesc().stream().map(mapper::toDoctorResponse).toList());
    }

    private List<DoctorTypeCountResponse> doctorsByType() {
        return List.of(
                new DoctorTypeCountResponse(DoctorType.GENERALIST, doctorRepository.countByType(DoctorType.GENERALIST)),
                new DoctorTypeCountResponse(DoctorType.SPECIALIST, doctorRepository.countByType(DoctorType.SPECIALIST)));
    }

    private List<MonthlyCountResponse> insuredMonthlyEvolution() {
        YearMonth current = YearMonth.now();
        YearMonth first = current.minusMonths(5);
        Map<String, InsuredPersonRepository.MonthlyCount> counts = insuredRepository
                .countByMonth(start(first), end(current))
                .stream().collect(Collectors.toMap(InsuredPersonRepository.MonthlyCount::getMonth, Function.identity()));
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5L - i))
                .map(month -> month.format(MONTH_FORMAT))
                .map(label -> new MonthlyCountResponse(label, counts.containsKey(label) ? counts.get(label).getCount() : 0))
                .toList();
    }

    private Instant startOfCurrentMonth() {
        return start(YearMonth.now());
    }

    private Instant start(YearMonth month) {
        return month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant end(YearMonth month) {
        return month.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Specification<com.csi.profile.domain.model.Doctor> doctorSpecification(DoctorType type, String specialty) {
        Specification<com.csi.profile.domain.model.Doctor> spec = Specification.where(null);
        if (type != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        if (specialty != null && !specialty.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("specialty")), specialty.trim().toUpperCase()));
        }
        return spec;
    }

    private InstantRange resolveRange(DashboardPeriod requested, LocalDate customStart, LocalDate customEnd) {
        DashboardPeriod period = requested == null ? DashboardPeriod.MONTH : requested;
        ZoneId zone = ZoneId.of("Africa/Douala");
        LocalDate today = LocalDate.now(zone);
        LocalDate startDate = switch (period) {
            case DAY -> today;
            case WEEK -> today.minusDays(today.getDayOfWeek().getValue() - 1L);
            case MONTH -> today.withDayOfMonth(1);
            case QUARTER -> today.minusMonths(2).withDayOfMonth(1);
            case YEAR -> today.withDayOfYear(1);
            case CUSTOM -> customStart == null ? today : customStart;
        };
        LocalDate endDate = period == DashboardPeriod.CUSTOM && customEnd != null ? customEnd : today;
        return new InstantRange(startDate.atStartOfDay(zone).toInstant(), endDate.plusDays(1).atStartOfDay(zone).toInstant());
    }

    private record InstantRange(Instant start, Instant end) {}
}

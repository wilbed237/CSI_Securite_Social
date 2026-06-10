package com.csi.reimbursement.application.service;

import com.csi.reimbursement.application.dto.DashboardDtos.*;
import com.csi.reimbursement.application.dto.DashboardPeriod;
import com.csi.reimbursement.application.mapper.ReimbursementMapper;
import com.csi.reimbursement.domain.model.Reimbursement;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;
import com.csi.reimbursement.infrastructure.persistence.ReimbursementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agregations locales des remboursements.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Douala");

    private final ReimbursementRepository reimbursementRepository;
    private final ReimbursementMapper mapper;

    @Transactional(readOnly = true)
    public ReimbursementStatsResponse reimbursementStats(DashboardPeriod period, LocalDate startDate, LocalDate endDate,
                                                          ReimbursementType type, ReimbursementStatus status, UUID currentUserId) {
        DateRange range = resolveRange(period, startDate, endDate);
        String typeFilter = type == null ? null : type.name();
        String statusFilter = status == null ? null : status.name();
        ReimbursementRepository.DashboardAggregate aggregate = reimbursementRepository.dashboardAggregate(
                range.start(), range.end(), typeFilter, statusFilter, currentUserId,
                LocalDate.now(BUSINESS_ZONE).minusDays(7));
        long total = aggregate.getTotal();
        long executed = aggregate.getExecuted();
        long pending = aggregate.getPending();
        long rejected = aggregate.getRejected();
        double validationRate = total == 0 ? 0 : (executed * 100.0) / total;
        double rejectionRate = total == 0 ? 0 : (rejected * 100.0) / total;
        List<Reimbursement> latest = reimbursementRepository.findLatest(
                range.start(), range.end(), type, status, PageRequest.of(0, 5));
        List<Reimbursement> latestExecuted = reimbursementRepository.findLatestExecuted(
                range.start(), range.end(), type, ReimbursementStatus.EXECUTED, PageRequest.of(0, 5));
        return new ReimbursementStatsResponse(
                total,
                executed,
                pending,
                rejected,
                value(aggregate.getTotalAmount()),
                value(aggregate.getAverageAmount()),
                value(aggregate.getMaximumAmount()),
                value(aggregate.getMinimumAmount()),
                value(aggregate.getAgentAmount()),
                aggregate.getAgentCount(),
                validationRate,
                rejectionRate,
                aggregate.getAverageDelayDays(),
                aggregate.getOverdue(),
                byStatus(executed, pending, rejected),
                byType(range, typeFilter, statusFilter),
                amountByMonth(range, typeFilter, statusFilter),
                monthlyEvolution(),
                latest.stream().map(mapper::toResponse).toList(),
                latestExecuted.stream().map(mapper::toResponse).toList());
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private List<TypeCountResponse> byType(DateRange range, String type, String status) {
        Map<String, Long> counts = reimbursementRepository.countByType(range.start(), range.end(), type, status)
                .stream().collect(Collectors.toMap(ReimbursementRepository.TypeAggregate::getType, ReimbursementRepository.TypeAggregate::getCount));
        return java.util.Arrays.stream(ReimbursementType.values())
                .map(item -> new TypeCountResponse(item, counts.getOrDefault(item.name(), 0L)))
                .toList();
    }

    private DateRange resolveRange(DashboardPeriod requested, LocalDate customStart, LocalDate customEnd) {
        DashboardPeriod period = requested == null ? DashboardPeriod.MONTH : requested;
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        return switch (period) {
            case DAY -> new DateRange(today, today);
            case WEEK -> new DateRange(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today);
            case MONTH -> new DateRange(today.withDayOfMonth(1), today);
            case QUARTER -> new DateRange(today.minusMonths(2).withDayOfMonth(1), today);
            case YEAR -> new DateRange(today.withDayOfYear(1), today);
            case CUSTOM -> new DateRange(customStart == null ? today : customStart, customEnd == null ? today : customEnd);
        };
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private List<StatusCountResponse> byStatus(long executed, long pending, long rejected) {
        return List.of(
                new StatusCountResponse(ReimbursementStatus.EXECUTED, executed),
                new StatusCountResponse(ReimbursementStatus.PENDING, pending),
                new StatusCountResponse(ReimbursementStatus.REJECTED, rejected));
    }

    private List<MonthlyAmountResponse> amountByMonth(DateRange range, String type, String status) {
        YearMonth current = YearMonth.now();
        Map<String, BigDecimal> amounts = reimbursementRepository.amountByMonth(range.start(), range.end(), type, status)
                .stream().collect(Collectors.toMap(ReimbursementRepository.MonthlyAmount::getMonth, ReimbursementRepository.MonthlyAmount::getAmount));
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5L - i))
                .map(month -> month.format(MONTH_FORMAT))
                .map(label -> new MonthlyAmountResponse(label, value(amounts.get(label))))
                .toList();
    }

    private List<MonthlyCountResponse> monthlyEvolution() {
        YearMonth current = YearMonth.now();
        YearMonth first = current.minusMonths(5);
        Map<String, ReimbursementRepository.MonthlyCount> counts = reimbursementRepository
                .countByMonth(start(first), current.plusMonths(1).atDay(1))
                .stream().collect(Collectors.toMap(ReimbursementRepository.MonthlyCount::getMonth, Function.identity()));
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5L - i))
                .map(month -> month.format(MONTH_FORMAT))
                .map(label -> new MonthlyCountResponse(label, counts.containsKey(label) ? counts.get(label).getCount() : 0))
                .toList();
    }

    private LocalDate start(YearMonth month) {
        return month.atDay(1);
    }

}

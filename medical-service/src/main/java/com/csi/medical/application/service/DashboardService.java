package com.csi.medical.application.service;

import com.csi.medical.application.dto.DashboardDtos.*;
import com.csi.medical.domain.model.PrescriptionType;
import com.csi.medical.infrastructure.persistence.ConsultationRepository;
import com.csi.medical.infrastructure.persistence.DiseaseSheetRepository;
import com.csi.medical.infrastructure.persistence.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agregations locales du dashboard medecin.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ConsultationRepository consultationRepository;
    private final DiseaseSheetRepository diseaseSheetRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Transactional(readOnly = true)
    public DoctorDashboardSummaryResponse summary(String doctorMatricule) {
        String matricule = normalize(doctorMatricule);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        YearMonth currentMonth = YearMonth.from(today);
        ConsultationRepository.DashboardCounts counts = consultationRepository.dashboardCounts(
                matricule,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay(),
                weekStart.atStartOfDay(), weekStart.plusDays(7).atStartOfDay(),
                currentMonth.atDay(1).atStartOfDay(), currentMonth.plusMonths(1).atDay(1).atStartOfDay());
        long consultations = counts.getTotal();
        long diseaseSheets = countDiseaseSheets(doctorMatricule);
        long medicationPrescriptions = countPrescriptions(doctorMatricule, PrescriptionType.MEDICATION);
        long recommendations = countPrescriptions(doctorMatricule, PrescriptionType.SPECIALIST_CONSULTATION);
        return new DoctorDashboardSummaryResponse(
                counts.getToday(),
                counts.getWeek(),
                counts.getMonth(),
                consultations,
                diseaseSheets,
                medicationPrescriptions,
                recommendations,
                consultations,
                0,
                consultationsByDay(doctorMatricule),
                diseaseSheetsByMonth(doctorMatricule),
                prescriptionsByCategory(doctorMatricule));
    }

    @Transactional(readOnly = true)
    public DoctorPatientsStatsResponse patientsStats(String doctorMatricule) {
        return new DoctorPatientsStatsResponse(patientsToday(doctorMatricule), patientsThisWeek(doctorMatricule), patientsThisMonth(doctorMatricule));
    }

    @Transactional(readOnly = true)
    public DoctorConsultationsStatsResponse consultationsStats(String doctorMatricule) {
        return new DoctorConsultationsStatsResponse(totalConsultations(doctorMatricule), consultationsByDay(doctorMatricule));
    }

    @Transactional(readOnly = true)
    public DoctorDiseaseSheetsStatsResponse diseaseSheetsStats(String doctorMatricule) {
        return new DoctorDiseaseSheetsStatsResponse(countDiseaseSheets(doctorMatricule), diseaseSheetsByMonth(doctorMatricule));
    }

    @Transactional(readOnly = true)
    public DoctorRecommendationsStatsResponse recommendationsStats(String doctorMatricule) {
        return new DoctorRecommendationsStatsResponse(countPrescriptions(doctorMatricule, PrescriptionType.SPECIALIST_CONSULTATION));
    }

    @Transactional(readOnly = true)
    public DoctorRecentActivitiesResponse recentActivities() {
        return new DoctorRecentActivitiesResponse(List.of());
    }

    private long patientsToday(String doctorMatricule) {
        LocalDate today = LocalDate.now();
        return countConsultations(doctorMatricule, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private long patientsThisWeek(String doctorMatricule) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        return countConsultations(doctorMatricule, start.atStartOfDay(), start.plusDays(7).atStartOfDay());
    }

    private long patientsThisMonth(String doctorMatricule) {
        YearMonth month = YearMonth.now();
        return countConsultations(doctorMatricule, month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay());
    }

    private long countConsultations(String doctorMatricule, LocalDateTime start, LocalDateTime end) {
        if (isBlank(doctorMatricule)) {
            return consultationRepository.countByStartedAtBetween(start, end);
        }
        return consultationRepository.countByDoctorMatriculeIgnoreCaseAndStartedAtBetween(doctorMatricule, start, end);
    }

    private long totalConsultations(String doctorMatricule) {
        if (isBlank(doctorMatricule)) {
            return consultationRepository.count();
        }
        return consultationRepository.countByDoctorMatriculeIgnoreCase(doctorMatricule);
    }

    private long countDiseaseSheets(String doctorMatricule) {
        if (isBlank(doctorMatricule)) {
            return diseaseSheetRepository.count();
        }
        return diseaseSheetRepository.countByConsultationDoctorMatriculeIgnoreCase(doctorMatricule);
    }

    private long countPrescriptions(String doctorMatricule, PrescriptionType type) {
        if (isBlank(doctorMatricule)) {
            return prescriptionRepository.countByType(type);
        }
        return prescriptionRepository.countByConsultationDoctorMatriculeIgnoreCaseAndType(doctorMatricule, type);
    }

    private List<MonthlyCountResponse> consultationsByDay(String doctorMatricule) {
        LocalDate today = LocalDate.now();
        Map<String, ConsultationRepository.PeriodCount> counts = consultationRepository
                .countByDay(normalize(doctorMatricule), today.minusDays(6).atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream().collect(Collectors.toMap(ConsultationRepository.PeriodCount::getLabel, Function.identity()));
        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> today.minusDays(6L - i))
                .map(day -> day.format(DAY_FORMAT))
                .map(label -> new MonthlyCountResponse(label, counts.containsKey(label) ? counts.get(label).getCount() : 0))
                .toList();
    }

    private List<MonthlyCountResponse> diseaseSheetsByMonth(String doctorMatricule) {
        YearMonth current = YearMonth.now();
        YearMonth first = current.minusMonths(5);
        Map<String, DiseaseSheetRepository.PeriodCount> counts = diseaseSheetRepository
                .countByMonth(normalize(doctorMatricule), first.atDay(1), current.plusMonths(1).atDay(1))
                .stream().collect(Collectors.toMap(DiseaseSheetRepository.PeriodCount::getLabel, Function.identity()));
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5L - i))
                .map(month -> month.format(MONTH_FORMAT))
                .map(label -> new MonthlyCountResponse(label, counts.containsKey(label) ? counts.get(label).getCount() : 0))
                .toList();
    }

    private List<CategoryCountResponse> prescriptionsByCategory(String doctorMatricule) {
        return List.of(
                new CategoryCountResponse("MEDICATION", countPrescriptions(doctorMatricule, PrescriptionType.MEDICATION)),
                new CategoryCountResponse("SPECIALIST_CONSULTATION", countPrescriptions(doctorMatricule, PrescriptionType.SPECIALIST_CONSULTATION)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }
}

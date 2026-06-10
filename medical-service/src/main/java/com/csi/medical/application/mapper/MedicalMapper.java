package com.csi.medical.application.mapper;

import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.domain.model.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface MedicalMapper {
    default ConsultationResponse toResponse(Consultation c) {
        return new ConsultationResponse(c.getId(), c.getInsuranceNumber(), c.getDoctorMatricule(), c.getDoctorType(),
                c.getStartedAt(), c.getEndedAt(), c.getCost(), c.getConsultationType(), c.getReason(),
                c.getObservations(), c.getDiagnosis(), c.getConclusion(), c.getStatus(), c.getCreatedAt(),
                c.getUpdatedAt(), c.getCreatedByUserId(), c.getUpdatedByUserId(), c.getVersion());
    }

    default MedicationResponse toResponse(Medication m) {
        return new MedicationResponse(m.getId(), m.getName(), m.getPosology(), m.getFrequency(), m.getDuration(),
                m.getQuantity(), m.getAdministrationRoute(), m.getInstructions());
    }

    default PrescriptionResponse toResponse(Prescription p) {
        List<MedicationResponse> items = p.getMedications() == null ? List.of() : p.getMedications().stream().map(this::toResponse).toList();
        Consultation c = p.getConsultation();
        return new PrescriptionResponse(p.getId(), p.getPrescriptionNumber(), p.getType(), p.getPrescriptionDate(),
                c.getId(), p.getDiseaseSheetId(), c.getInsuranceNumber(), c.getDoctorMatricule(),
                p.getRequiredSpecialty(), p.getFactors(), p.getNotes(), p.getStatus(), items, items.size(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getCreatedByUserId(), p.getUpdatedByUserId(), p.getVersion());
    }

    default DiseaseSheetResponse toResponse(DiseaseSheet d) {
        return new DiseaseSheetResponse(d.getId(), d.getSheetNumber(), d.getDate(), d.getDiagnosis(),
                d.getMedicalConclusion(), d.getStatus(), d.getConsultation().getId(), d.getPrescriptionId(),
                d.getPatientId(), d.getDoctorId(), d.getDoctorType(), d.getSpecialty(), d.getConsultationAmount(),
                d.getConsultationDate(), d.getRegistrationDate(), d.getReceivedAt(), d.getPaymentType(),
                d.getControlComment(), d.getCompletedByUserId(), d.getCompletedAt(), d.getReimbursementId(),
                d.getReimbursementNumber(), d.getCreatedAt(), d.getUpdatedAt(), d.getCreatedByUserId(),
                d.getUpdatedByUserId(), d.getVersion());
    }
}

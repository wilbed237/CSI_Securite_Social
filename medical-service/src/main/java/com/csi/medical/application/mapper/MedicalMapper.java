package com.csi.medical.application.mapper;

import com.csi.medical.application.dto.MedicalDtos.*;
import com.csi.medical.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
/**
 * Mapper MapStruct du domaine medical vers les DTO REST.
 */
public interface MedicalMapper {
    ConsultationResponse toResponse(Consultation consultation);
    MedicationResponse toResponse(Medication medication);

    @Mapping(target = "consultationId", source = "consultation.id")
    PrescriptionResponse toResponse(Prescription prescription);

    @Mapping(target = "consultationId", source = "consultation.id")
    @Mapping(target = "insuranceNumber", source = "consultation.insuranceNumber")
    @Mapping(target = "doctorMatricule", source = "consultation.doctorMatricule")
    @Mapping(target = "doctorType", source = "consultation.doctorType")
    @Mapping(target = "consultationCost", source = "consultation.cost")
    DiseaseSheetResponse toResponse(DiseaseSheet diseaseSheet);
}

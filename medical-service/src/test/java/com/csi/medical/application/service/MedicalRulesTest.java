package com.csi.medical.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.medical.application.dto.MedicalDtos.CreateConsultationRequest;
import com.csi.medical.application.mapper.MedicalMapper;
import com.csi.medical.domain.model.DoctorType;
import com.csi.medical.infrastructure.client.ProfileClient;
import com.csi.medical.infrastructure.persistence.ConsultationRepository;
import com.csi.medical.infrastructure.persistence.DiseaseSheetRepository;
import com.csi.medical.infrastructure.persistence.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class MedicalRulesTest {
    @Test
    void consultationEndMustBeAfterStart() {
        MedicalService service = new MedicalService(mock(ConsultationRepository.class), mock(PrescriptionRepository.class), mock(DiseaseSheetRepository.class), Mappers.getMapper(MedicalMapper.class), mock(ProfileClient.class));
        LocalDateTime now = LocalDateTime.now();
        var request = new CreateConsultationRequest("ASS-1", "MED-1", DoctorType.GENERALIST, now, now.minusMinutes(1), BigDecimal.TEN);
        assertThatThrownBy(() -> service.createConsultation(request)).isInstanceOf(BusinessException.class);
    }
}

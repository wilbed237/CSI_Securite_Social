package com.csi.profile.application.service;

import com.csi.profile.application.dto.ProfileDtos.CreateDoctorRequest;
import com.csi.profile.domain.model.DoctorType;
import com.csi.profile.infrastructure.persistence.DoctorRepository;
import com.csi.profile.infrastructure.persistence.InsuredPersonRepository;
import com.csi.profile.infrastructure.persistence.SocialAgentRepository;
import com.csi.profile.infrastructure.persistence.PrimaryDoctorAssignmentRepository;
import com.csi.profile.application.mapper.ProfileMapper;
import com.csi.common.domain.BusinessException;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ProfileRulesTest {
    @Test
    void specialistRequiresSpecialty() {
        ProfileService service = new ProfileService(mock(InsuredPersonRepository.class), mock(DoctorRepository.class), mock(SocialAgentRepository.class),
                mock(PrimaryDoctorAssignmentRepository.class), Mappers.getMapper(ProfileMapper.class),
                new SensitiveDataCipher("test-sensitive-data-key"), mock(AuditService.class));
        CreateDoctorRequest request = new CreateDoctorRequest("M1", "A", "B", DoctorType.SPECIALIST, null, null, "a@b.com");
        assertThatThrownBy(() -> service.registerDoctor(request)).isInstanceOf(BusinessException.class);
    }
}

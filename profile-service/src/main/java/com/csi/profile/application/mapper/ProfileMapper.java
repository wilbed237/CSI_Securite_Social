package com.csi.profile.application.mapper;

import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.InsuredPerson;
import com.csi.profile.domain.model.SocialAgent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
/**
 * Mapper MapStruct entre entites JPA du profil et DTO REST.
 */
public interface ProfileMapper {
    DoctorResponse toDoctorResponse(Doctor doctor);
    SocialAgentResponse toSocialAgentResponse(SocialAgent agent);
    @Mapping(target = "bankAccountMasked", ignore = true)
    InsuredResponse toInsuredResponse(InsuredPerson insuredPerson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "treatingDoctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bankAccountEncrypted", ignore = true)
    InsuredPerson toEntity(CreateInsuredRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authUserId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);
}

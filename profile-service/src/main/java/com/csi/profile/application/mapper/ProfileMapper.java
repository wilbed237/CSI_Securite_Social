package com.csi.profile.application.mapper;

import com.csi.profile.application.dto.ProfileDtos.*;
import com.csi.profile.domain.model.Doctor;
import com.csi.profile.domain.model.InsuredPerson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
/**
 * Mapper MapStruct entre entites JPA du profil et DTO REST.
 */
public interface ProfileMapper {
    DoctorResponse toDoctorResponse(Doctor doctor);
    InsuredResponse toInsuredResponse(InsuredPerson insuredPerson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "treatingDoctor", ignore = true)
    InsuredPerson toEntity(CreateInsuredRequest request);

    @Mapping(target = "id", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);
}

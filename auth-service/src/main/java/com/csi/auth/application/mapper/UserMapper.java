package com.csi.auth.application.mapper;

import com.csi.auth.application.dto.AuthDtos.UserResponse;
import com.csi.auth.domain.model.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(UserAccount userAccount);
}

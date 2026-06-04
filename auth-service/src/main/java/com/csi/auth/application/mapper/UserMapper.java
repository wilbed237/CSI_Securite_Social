package com.csi.auth.application.mapper;

import com.csi.auth.application.dto.AuthDtos.UserResponse;
import com.csi.auth.domain.model.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
/**
 * Convertit les entites utilisateurs en DTO exposes par l API.
 */
public interface UserMapper {
    UserResponse toResponse(UserAccount userAccount);
}

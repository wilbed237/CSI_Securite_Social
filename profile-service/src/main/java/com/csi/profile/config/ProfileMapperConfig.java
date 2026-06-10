package com.csi.profile.config;

import com.csi.profile.application.mapper.ProfileMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileMapperConfig {
    @Bean
    ProfileMapper profileMapper() {
        return Mappers.getMapper(ProfileMapper.class);
    }
}

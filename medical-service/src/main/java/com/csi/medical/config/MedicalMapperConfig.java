package com.csi.medical.config;

import com.csi.medical.application.mapper.MedicalMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicalMapperConfig {
    @Bean
    MedicalMapper medicalMapper() {
        return Mappers.getMapper(MedicalMapper.class);
    }
}

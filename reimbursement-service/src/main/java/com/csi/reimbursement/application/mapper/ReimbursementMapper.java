package com.csi.reimbursement.application.mapper;

import com.csi.reimbursement.application.dto.ReimbursementDtos.ReimbursementResponse;
import com.csi.reimbursement.domain.model.Reimbursement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
/**
 * Mapper MapStruct entre l entite remboursement et la reponse REST.
 */
public interface ReimbursementMapper {
    ReimbursementResponse toResponse(Reimbursement reimbursement);
}

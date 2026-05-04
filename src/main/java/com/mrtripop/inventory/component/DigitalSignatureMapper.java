package com.mrtripop.inventory.component;

import com.mrtripop.inventory.models.db.DigitalSignature;
import com.mrtripop.inventory.models.dto.DigitalSignatureDto;
import com.mrtripop.inventory.models.dto.SignatureVerificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DigitalSignatureMapper {

  @Mapping(source = "storeStock.id", target = "storeStockId")
  @Mapping(source = "verificationStatus", target = "verificationStatus")
  DigitalSignatureDto toDto(DigitalSignature digitalSignature);

  @Mapping(source = "pharmacistLicenseNumber", target = "licenseNumber")
  @Mapping(source = "verificationStatus", target = "verificationStatus")
  SignatureVerificationDto toSignatureVerificationDto(DigitalSignature digitalSignature);
}

package com.mrtripop.location.utils;

import com.mrtripop.location.models.dtos.AddressDTO;
import com.mrtripop.location.models.entities.Address;

public class AddressUtil {
  private AddressUtil() {}

  public static Address dtoToAddress(AddressDTO addressDTO) {
    return Address.builder()
        .id(addressDTO.getId())
        .addressName(addressDTO.getAddressName())
        .line1(addressDTO.getLine1())
        .line2(addressDTO.getLine2())
        .city(addressDTO.getCity())
        .province(addressDTO.getProvince())
        .country(addressDTO.getCountry())
        .postalCode(addressDTO.getPostalCode())
        .warehouses(addressDTO.getWarehouseList())
        .build();
  }

  public static AddressDTO addressToDTO(Address addressDTO) {
    return AddressDTO.builder()
        .id(addressDTO.getId())
        .addressName(addressDTO.getAddressName())
        .line1(addressDTO.getLine1())
        .line2(addressDTO.getLine2())
        .city(addressDTO.getCity())
        .province(addressDTO.getProvince())
        .country(addressDTO.getCountry())
        .postalCode(addressDTO.getPostalCode())
        .warehouseList(addressDTO.getWarehouses())
        .build();
  }

  public static void updateAddress(Address oldAddress, AddressDTO newAddress) {
    oldAddress.setAddressName(newAddress.getAddressName());
    oldAddress.setLine1(newAddress.getLine1());
    oldAddress.setLine2(newAddress.getLine2());
    oldAddress.setCity(newAddress.getCity());
    oldAddress.setProvince(newAddress.getProvince());
    oldAddress.setCountry(newAddress.getCountry());
    oldAddress.setPostalCode(newAddress.getPostalCode());
    oldAddress.setWarehouses(newAddress.getWarehouseList());
  }
}

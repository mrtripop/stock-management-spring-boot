package com.mrtripop.location.models.dtos;

import com.mrtripop.location.models.entities.Address;
import com.mrtripop.location.models.entities.Warehouse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

  private Long id;

  @NotNull(message = "Address name must not be null")
  private String addressName;

  @NotNull(message = "Address line 1 must not be null")
  private String line1;

  @NotNull(message = "Address line 2 must not be null")
  private String line2;

  @NotNull(message = "Address city must not be null")
  private String city;

  @NotNull(message = "Address province must not be null")
  private String province;

  @NotNull(message = "Address country must not be null")
  private String country;

  @NotNull(message = "Address postal code must not be null")
  private String postalCode;

  private List<Warehouse> warehouseList;
}

package com.mrtripop.product.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {

  @Null(message = "Request body ID should be null")
  private Long id;

  @NotEmpty(message = "Product code(SKU) must not be empty")
  @NotBlank(message = "Product code(SKU) must not be blank")
  @NotNull(message = "Product code(SKU) must not be null")
  private String code;

  @NotEmpty(message = "Barcode must not be empty")
  @NotBlank(message = "Barcode must not be blank")
  @NotNull(message = "Barcode must not be null")
  private String barcode;

  @NotEmpty(message = "Product name must not be empty")
  @NotBlank(message = "Product name must not be blank")
  @NotNull(message = "Product name must not be null")
  private String name;

  @NotEmpty(message = "Description must not be empty")
  @NotBlank(message = "Description must not be blank")
  @NotNull(message = "Description must not be null")
  @Length(max = 300, message = "Description is maximum with 300 character")
  private String description;

  @NotEmpty(message = "Category must not be empty")
  @NotBlank(message = "Category must not be blank")
  @NotNull(message = "Category must not be null")
  private String category;

  @Min(value = 0, message = "Re-order quantity minimum is zero")
  @NotNull(message = "Re-order quantity must not be null")
  private Integer reorderQuantity;

  @Min(value = 0, message = "Packed weight minimum is zero")
  @NotNull(message = "Packed weight must not be null")
  private Double packedWeight;

  @Min(value = 0, message = "Packed height minimum is zero")
  @NotNull(message = "Packed height must not be null")
  private Double packedHeight;

  @Min(value = 0, message = "Packed width minimum is zero")
  @NotNull(message = "Packed width must not be null")
  private Double packedWidth;

  @Min(value = 0, message = "Packed depth minimum is zero")
  @NotNull(message = "Packed depth must not be null")
  private Double packedDepth;

  @NotNull(message = "Is active flag must not be null")
  private Boolean isActive;
}

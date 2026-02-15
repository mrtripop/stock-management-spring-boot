package com.mrtripop.product.models.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "product",
    indexes = {
      @Index(name = "product_code", columnList = "code"),
      @Index(name = "product_created_at", columnList = "created_at"),
      @Index(name = "product_updated_at", columnList = "updated_at"),
    })
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseProduct {}

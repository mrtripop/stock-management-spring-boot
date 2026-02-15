package com.mrtripop.product.models.db;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Table(
    name = "product",
    indexes = {
      @Index(name = "product_code", columnList = "code"),
      @Index(name = "product_created_at", columnList = "created_at"),
      @Index(name = "product_updated_at", columnList = "updated_at"),
    })
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseProduct {}

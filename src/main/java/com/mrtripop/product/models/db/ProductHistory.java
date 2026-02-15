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
    name = "product_history",
    indexes = {
      @Index(name = "product_history_created_at", columnList = "created_at"),
    })
@EqualsAndHashCode(callSuper = true)
public class ProductHistory extends BaseProduct {}

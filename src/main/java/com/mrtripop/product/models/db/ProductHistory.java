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
    name = "product_history",
    indexes = {
      @Index(name = "product_history_created_at", columnList = "created_at"),
    })
@EqualsAndHashCode(callSuper = true)
public class ProductHistory extends BaseProduct {}

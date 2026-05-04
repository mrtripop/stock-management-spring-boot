package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "unit_conversions",
    indexes = {
      @Index(name = "uc_brand_from_unit", columnList = "brand_id, from_unit", unique = true)
    }
)
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversion extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unit_conversion_seq")
  @SequenceGenerator(
      name = "unit_conversion_seq",
      sequenceName = "unit_conversion_seq",
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "from_unit", nullable = false, length = 50)
  private String fromUnit;

  @Column(name = "to_unit", nullable = false, length = 50)
  private String toUnit;

  @Column(name = "ratio", nullable = false)
  private Integer ratio;
}

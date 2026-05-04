package com.mrtripop.clinical.models.db;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = {"store", "brand"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "store_products",
    uniqueConstraints =
        @UniqueConstraint(
            name = "idx_store_products_store_brand",
            columnNames = {"store_id", "brand_id"}),
    indexes = {
      @Index(name = "idx_store_products_store_active", columnList = "store_id, is_active")
    })
public class StoreProduct extends AuditEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "price", precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "shelf_location", length = 100)
  private String shelfLocation;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "reorder_threshold")
  private Long reorderThreshold;
}

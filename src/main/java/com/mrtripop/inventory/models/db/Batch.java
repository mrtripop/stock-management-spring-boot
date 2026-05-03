package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "batches",
    indexes = {
      @Index(name = "idx_batches_brand_batch", columnList = "brand_id, batch_number", unique = true),
      @Index(name = "idx_batches_expiry_date", columnList = "expiry_date"),
      @Index(name = "idx_batches_status", columnList = "status")
    }
)
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Batch extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batches_sequence")
  @SequenceGenerator(
      name = "batches_sequence",
      sequenceName = "batches_sequence",
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "batch_number", nullable = false, length = 100)
  private String batchNumber;

  @Column(name = "expiry_date", nullable = false)
  private LocalDate expiryDate;

  @Column(name = "quantity", nullable = false)
  private Long quantity = 0L;

  @Column(name = "supplier_reference", length = 200)
  private String supplierReference;

  @Column(name = "manufacturer_lot_number", length = 200)
  private String manufacturerLotNumber;

  @Column(name = "storage_conditions", length = 200)
  private String storageConditions;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private BatchStatus status = BatchStatus.AVAILABLE;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;
}
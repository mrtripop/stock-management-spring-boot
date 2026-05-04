package com.mrtripop.inventory.models.db;

import com.mrtripop.clinical.models.db.Store;
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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "store_stocks",
    indexes = {
      @Index(name = "idx_store_stocks_store_batch", columnList = "store_id, batch_id", unique = true),
      @Index(name = "idx_store_stocks_batch_id", columnList = "batch_id")
    }
)
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StoreStock extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "store_stocks_sequence")
  @SequenceGenerator(
      name = "store_stocks_sequence",
      sequenceName = "store_stocks_sequence",
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "batch_id", nullable = false)
  private Batch batch;

  @Column(name = "quantity", nullable = false)
  private Long quantity = 0L;

  @Column(name = "location", length = 100)
  private String location;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;
}
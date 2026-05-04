package com.mrtripop.transaction.models.db;

import com.mrtripop.clinical.models.db.Store;
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
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "invoices",
    indexes = {
      @Index(name = "idx_invoices_store_id", columnList = "store_id"),
      @Index(name = "idx_invoices_status", columnList = "status")
    })
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoices_sequence")
  @SequenceGenerator(
      name = "invoices_sequence",
      sequenceName = "invoices_sequence",
      allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private InvoiceStatus status = InvoiceStatus.PENDING;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "patient_owed", nullable = false, precision = 10, scale = 2)
  private BigDecimal patientOwed;

  @Column(name = "insurance_claim_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal insuranceClaimAmount;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;
}

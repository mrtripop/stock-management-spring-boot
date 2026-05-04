package com.mrtripop.transaction.models.db;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.inventory.models.db.Batch;
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
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "invoice_items",
    indexes = {@Index(name = "idx_invoice_items_invoice_id", columnList = "invoice_id")})
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"invoice"})
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_items_sequence")
  @SequenceGenerator(
      name = "invoice_items_sequence",
      sequenceName = "invoice_items_sequence",
      allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "batch_id", nullable = false)
  private Batch batch;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "patient_owed", nullable = false, precision = 10, scale = 2)
  private BigDecimal patientOwed;

  @Column(name = "insurance_claim_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal insuranceClaimAmount;

  @Column(name = "insurance_coverage_percent", nullable = false)
  private Integer insuranceCoveragePercent = 0;
}

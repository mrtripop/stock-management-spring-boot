package com.mrtripop.transaction.models.db;

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
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "returns",
    indexes = {@Index(name = "idx_returns_invoice_id", columnList = "invoice_id")})
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Return extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "returns_sequence")
  @SequenceGenerator(name = "returns_sequence", sequenceName = "returns_sequence", allocationSize = 1)
  private Long id;

  @Comment("FK to invoices; the completed invoice this return is recorded against")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Comment("Why this return was recorded — damaged, expired, wrong item, customer changed "
      + "mind, or other")
  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false, length = 30)
  private ReturnReason reason;

  // Sum of every ReturnItem.refundAmount under this Return; always positive.
  @Comment("Total refund amount across all items in this return")
  @Column(name = "total_refund_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalRefundAmount;
}

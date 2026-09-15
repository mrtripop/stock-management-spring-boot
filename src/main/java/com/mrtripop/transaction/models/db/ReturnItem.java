package com.mrtripop.transaction.models.db;

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
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "return_items",
    indexes = {
      @Index(name = "idx_return_items_return_id", columnList = "return_id"),
      @Index(name = "idx_return_items_invoice_item_id", columnList = "invoice_item_id")
    })
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"parentReturn"})
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_items_sequence")
  @SequenceGenerator(
      name = "return_items_sequence",
      sequenceName = "return_items_sequence",
      allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_id", nullable = false)
  private Return parentReturn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_item_id", nullable = false)
  private InvoiceItem invoiceItem;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  // Refund for this line only: invoiceItem.unitPrice * quantity returned.
  @Comment("Refund amount for this single returned line item")
  @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal refundAmount;
}

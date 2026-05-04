package com.mrtripop.inventory.models.db;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "digital_signatures",
    indexes = {@Index(name = "ds_store_stock", columnList = "store_stock_id")}
)
@SuperBuilder
@Getter
@Setter
@ToString(exclude = {"storeStock"})
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignature extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "digital_signature_seq")
  @SequenceGenerator(
      name = "digital_signature_seq",
      sequenceName = "digital_signature_seq",
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_stock_id", nullable = false)
  private StoreStock storeStock;

  @Column(name = "pharmacist_license_number", nullable = false, length = 30)
  private String pharmacistLicenseNumber;

  @Column(name = "signature_payload", columnDefinition = "text")
  private String signaturePayload;

  @Column(name = "signature_hash", length = 64)
  private String signatureHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", nullable = false, length = 20)
  private VerificationStatus verificationStatus;

  @Column(name = "verified_at", nullable = false)
  private Long verifiedAt;
}

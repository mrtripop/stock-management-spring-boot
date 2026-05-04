package com.mrtripop.clinical.models.db;

import com.mrtripop.product.models.db.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "molecules")
public class Molecule extends AuditEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "generic_name", nullable = false)
  private String genericName;

  @Column(name = "therapeutic_class")
  private String therapeuticClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "regulatory_schedule")
  private RegulatorySchedule regulatorySchedule;

  @Column(name = "dosage_instructions", columnDefinition = "TEXT")
  private String dosageInstructions;

  @Column(name = "safety_warnings", columnDefinition = "TEXT")
  private String safetyWarnings;
}

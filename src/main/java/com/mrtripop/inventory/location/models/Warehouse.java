package com.mrtripop.inventory.location.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouses")
@EntityListeners(AuditingEntityListener.class)
public class Warehouse {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_id_seq")
  @SequenceGenerator(name = "warehouse_id_seq", allocationSize = 1)
  @Column(name = "warehouse_id", columnDefinition = "BIGINT")
  private Long warehouseId;

  @Column(name = "warehouse_name", columnDefinition = "TEXT")
  private String warehouseName;

  @Column(name = "is_refrigerated", columnDefinition = "BOOLEAN")
  private Boolean isRefrigerated;

  @ManyToOne
  @JoinColumn(name = "address_id")
  private Address address;

  @CreatedDate
  @Column(name = "created_at", columnDefinition = "BIGINT")
  private Long createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", columnDefinition = "BIGINT")
  private Long updatedAt;
}

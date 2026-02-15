package com.mrtripop.product.models.db;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity {
  /** CreatedAt: Time that product is created */
  @CreatedDate
  @Column(name = "created_at", columnDefinition = "BIGINT")
  private Long createdAt;

  /** UpdatedAt: Time that product is updated */
  @LastModifiedDate
  @Column(name = "updated_at", columnDefinition = "BIGINT")
  private Long updatedAt;
}

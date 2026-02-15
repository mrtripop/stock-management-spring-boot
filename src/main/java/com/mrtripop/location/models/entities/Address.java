package com.mrtripop.location.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
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
@Table(
    name = "addresses",
    indexes = {
      @Index(name = "addresses_created_at", columnList = "created_at"),
      @Index(name = "addresses_updated_at", columnList = "updated_at"),
    })
@EntityListeners(AuditingEntityListener.class)
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "addresses_id_seq")
  @SequenceGenerator(name = "addresses_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "address_name", columnDefinition = "TEXT")
  private String addressName;

  @Column(name = "line1", columnDefinition = "TEXT")
  private String line1;

  @Column(name = "line2", columnDefinition = "TEXT")
  private String line2;

  @Column(name = "city", columnDefinition = "TEXT")
  private String city;

  @Column(name = "province", columnDefinition = "TEXT")
  private String province;

  @Column(name = "country", columnDefinition = "TEXT")
  private String country;

  @Column(name = "postal_code", columnDefinition = "TEXT")
  private String postalCode;

//  @JsonIgnore
  @OneToMany(mappedBy = "address", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Warehouse> warehouses;

  @JsonIgnore
  @CreatedDate
  @Column(name = "created_at", columnDefinition = "BIGINT")
  private Long createdAt;

  @JsonIgnore
  @LastModifiedDate
  @Column(name = "updated_at", columnDefinition = "BIGINT")
  private Long updatedAt;
}

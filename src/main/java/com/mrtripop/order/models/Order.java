package com.mrtripop.order.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mrtripop.users.models.User;
import com.mrtripop.location.models.entities.Address;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "order_info")
@NoArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_info_seq")
  @SequenceGenerator(name = "order_info_seq", allocationSize = 1)
  private Long id;

  @Column(columnDefinition = "smallint")
  private Integer type;

  @Column(columnDefinition = "smallint")
  private Integer status;

  private Float subTotal;
  private Float itemDiscount;
  private Float tax;
  private Float shipping;
  private Float total;
  private Float discount;
  private Float grandTotal;

  @Column(columnDefinition = "varchar(50)")
  private String promo;

  @Column(columnDefinition = "text")
  private String content;

  @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
  private ZonedDateTime createdAt;

  @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
  private ZonedDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "address_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private Address address;
}

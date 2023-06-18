package com.learn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "order_info")
@NoArgsConstructor
public class Order implements Serializable {

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

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;
}

package com.mrtripop.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mrtripop.users.models.User;
import com.mrtripop.order.models.Order;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "transaction")
@NoArgsConstructor
public class Transaction implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
  @SequenceGenerator(name = "transaction_seq", allocationSize = 1)
  private Long id;

  @Column(columnDefinition = "varchar(100)")
  private String code;

  @Column(columnDefinition = "smallint")
  private Integer type;

  @Column(columnDefinition = "smallint")
  private Integer mode;

  @Column(columnDefinition = "smallint")
  private Integer status;

  @Column(columnDefinition = "text")
  private String content;

  @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
  private ZonedDateTime createdAt;

  @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
  private ZonedDateTime updatedAt;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private User user;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private Order order;
}

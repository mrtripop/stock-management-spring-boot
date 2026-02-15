package com.mrtripop.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_balances")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "user_balances_seq", allocationSize = 1)
  private Long id;

  private String name;
  private Double balance;
}

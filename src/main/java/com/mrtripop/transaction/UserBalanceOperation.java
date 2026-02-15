package com.mrtripop.transaction;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserBalanceOperation {

  private final UserBalance userBalance;

  public UserBalanceOperation(UserBalance userBalance) {
    this.userBalance = userBalance;
  }

  public UserBalance deposit(double amount) throws Exception {
    validateInputNumber(amount);
    BigDecimal userBalanceBigDecimal = BigDecimal.valueOf(userBalance.getBalance());
    BigDecimal userDepositAmount = BigDecimal.valueOf(amount);
    double updatedBalance = userBalanceBigDecimal.add(userDepositAmount).doubleValue();
    return new UserBalance(userBalance.getId(), userBalance.getName(), updatedBalance);
  }

  public UserBalance withdraw(double amount) throws Exception {
    validateInputNumber(amount);
    inputCanWithdraw(amount);
    BigDecimal userBalanceBigDecimal = BigDecimal.valueOf(userBalance.getBalance());
    BigDecimal userDepositAmount = BigDecimal.valueOf(amount);
    double updatedBalance = userBalanceBigDecimal.subtract(userDepositAmount).doubleValue();
    return new UserBalance(userBalance.getId(), userBalance.getName(), updatedBalance);
  }

  private void validateInputNumber(double amount) throws Exception {
    if (amount < 0) throw new Exception("User deposit/withdrawal amount is negative");
    if (amount == 0) throw new Exception("User deposit/withdrawal amount cannot be zero");
  }

  private void inputCanWithdraw(double amount) throws Exception {
    if (userBalance.getBalance() < amount) {
      throw new Exception("Current user balance cannot withdraw for " + amount + " baths");
    }
  }
}

package com.mrtripop.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mrtripop.transaction.UserBalance;
import com.mrtripop.transaction.UserBalanceOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@ExtendWith(MockitoExtension.class)
public class BalanceCalculationTest {
  @Mock private UserBalance currentBalance;

  @InjectMocks private UserBalanceOperation userBalanceOperation;

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.1, deposit 0.2 baths")
  void success_the_user_current_balance_is_0_1_deposit_0_2_baths() throws Exception {
    // arrange
    double userDeposit = 0.2;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.1);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.deposit(userDeposit);
    // assert
    assertEquals(0.3, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.1, deposit 0.7 baths")
  void success_the_user_current_balance_is_0_1_deposit_0_7_baths() throws Exception {
    // arrange
    double userDeposit = 0.7;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.1);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.deposit(userDeposit);
    // assert
    assertEquals(0.8, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.7, deposit 0.2 baths")
  void success_the_user_current_balance_is_0_7_deposit_0_2_baths() throws Exception {
    // arrange
    double userDeposit = 0.2;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.7);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.deposit(userDeposit);
    // assert
    assertEquals(0.9, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.3, withdraw 0.1 baths")
  void success_the_user_current_balance_is_0_3_withdraw_0_1_baths() throws Exception {
    // arrange
    double userWithdraw = 0.1;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.3);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.withdraw(userWithdraw);
    // assert
    assertEquals(0.2, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.5, withdraw 0.4 baths")
  void success_the_user_current_balance_is_0_5_withdraw_0_4_baths() throws Exception {
    // arrange
    double userWithdraw = 0.4;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.5);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.withdraw(userWithdraw);
    // assert
    assertEquals(0.1, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0.8, withdraw 0.6 baths")
  void success_the_user_current_balance_is_0_8_withdraw_0_6_baths() throws Exception {
    // arrange
    double userWithdraw = 0.6;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.8);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.withdraw(userWithdraw);
    // assert
    assertEquals(0.2, updatedUserBalance.getBalance());
  }

  @Test
  void test(){
    int a = 5;
    int b = 2;
    int c = a / b;
    assertEquals(2, c);
  }


}

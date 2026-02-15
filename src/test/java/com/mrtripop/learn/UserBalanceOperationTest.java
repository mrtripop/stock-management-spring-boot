package com.mrtripop.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mrtripop.transaction.UserBalance;
import com.mrtripop.transaction.UserBalanceOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserBalanceOperationTest {

  @Mock private UserBalance currentBalance;

  @InjectMocks private UserBalanceOperation userBalanceOperation;

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 0, deposit 200 baths")
  void success_the_user_current_balance_is_0_deposit_200_baths() throws Exception {
    // arrange
    double userDeposit = 200.0;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.0);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.deposit(userDeposit);
    // assert
    assertEquals(200.0, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[FAILED] The user's deposit -50 baths, receive error response")
  void failed_the_user_deposit_minus_50_baths_receive_error_response() {
    // arrange
    double userDeposit = -50.0;
    // action
    Exception exception =
        assertThrows(Exception.class, () -> userBalanceOperation.deposit(userDeposit));
    // assert
    assertEquals("User deposit/withdrawal amount is negative", exception.getMessage());
  }

  @Test
  @DisplayName("[FAILED] The user's deposit 0 baths, receive error response")
  void failed_the_user_deposit_0_baths_receive_error_response() {
    // arrange
    double userDeposit = 0.0;
    // action
    Exception exception =
        assertThrows(Exception.class, () -> userBalanceOperation.deposit(userDeposit));
    // assert
    assertEquals("User deposit/withdrawal amount cannot be zero", exception.getMessage());
  }

  @Test
  @DisplayName("[SUCCESS] The user's current balance is 200, withdraw 50 baths")
  void success_the_user_current_balance_is_200_withdraw_50_baths() throws Exception {
    // arrange
    double userWithdraw = 50.0;
    // stub
    when(currentBalance.getBalance()).thenReturn(200.0);
    // action
    UserBalance updatedUserBalance = userBalanceOperation.withdraw(userWithdraw);
    // assert
    assertEquals(150.0, updatedUserBalance.getBalance());
  }

  @Test
  @DisplayName("[FAILED] The user's withdraw -50 baths, receive error response")
  void failed_the_user_withdraw_minus_50_baths_receive_error_response() {
    // arrange
    double userWithdraw = -50.0;
    // action
    Exception exception =
        assertThrows(Exception.class, () -> userBalanceOperation.withdraw(userWithdraw));
    // assert
    assertEquals("User deposit/withdrawal amount is negative", exception.getMessage());
  }

  @Test
  @DisplayName("[FAILED] The user's current balance is 0, withdraw 50 baths")
  void failed_the_user_current_balance_is_0_withdraw_50_baths() {
    // arrange
    double userWithdraw = 50.0;
    // stub
    when(currentBalance.getBalance()).thenReturn(0.0);
    // action
    Exception exception =
        assertThrows(Exception.class, () -> userBalanceOperation.withdraw(userWithdraw));
    // assert
    assertEquals("Current user balance cannot withdraw for 50.0 baths", exception.getMessage());
  }

  @Test
  @DisplayName("[FAILED] The user's withdraw 0 baths, receive error response")
  void failed_the_user_withdraw_0_baths_receive_error_response() {
    // arrange
    double userWithdraw = 0.0;
    // action
    Exception exception =
        assertThrows(Exception.class, () -> userBalanceOperation.withdraw(userWithdraw));
    // assert
    assertEquals("User deposit/withdrawal amount cannot be zero", exception.getMessage());
  }
}

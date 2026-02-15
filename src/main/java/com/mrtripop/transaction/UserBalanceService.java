package com.mrtripop.transaction;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserBalanceService {

  private final UserBalanceRepository userBalanceRepository;

  public UserBalanceService(UserBalanceRepository userBalanceRepository) {
    this.userBalanceRepository = userBalanceRepository;
  }

  public UserBalance deposit(Long userId, Double amount) throws Exception {
    UserBalance userBalance = findUserBalanceById(userId);
    UserBalanceOperation userOperation = new UserBalanceOperation(userBalance);
    UserBalance updatedUserBalance = userOperation.deposit(amount);
    return userBalanceRepository.save(updatedUserBalance);
  }

  public UserBalance withdraw(Long userId, Double amount) throws Exception {
    UserBalance userBalance = findUserBalanceById(userId);
    UserBalanceOperation userOperation = new UserBalanceOperation(userBalance);
    UserBalance updatedUserBalance = userOperation.withdraw(amount);
    return userBalanceRepository.save(updatedUserBalance);
  }

  private UserBalance findUserBalanceById(Long userId) {
    Optional<UserBalance> currentUserBalance = userBalanceRepository.findById(userId);
    return currentUserBalance.orElse(defaultUserBalance());
  }

  private UserBalance defaultUserBalance() {
    return UserBalance.builder().name("default_user").balance(0.0).build();
  }
}

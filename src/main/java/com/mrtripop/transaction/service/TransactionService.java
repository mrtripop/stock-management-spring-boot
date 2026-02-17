package com.mrtripop.transaction.service;

import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.transaction.models.Transaction;
import com.mrtripop.transaction.repository.TransactionRepository;
import com.mrtripop.util.DatabaseHelper;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public List<Transaction> getTransactions(BaseQueryParams queryParams) {
    try {
      Pageable pageable = DatabaseHelper.initPageableWithSort(queryParams);
      Page<Transaction> transactionPage = transactionRepository.findAll(pageable);
      return transactionPage.getContent();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Transaction> getTransactionByUserId(Long userId) {
    try {
      Optional<List<Transaction>> transactions = transactionRepository.findByUserId(userId);
      return transactions.orElse(List.of());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Transaction getTransactionById(Long transactionId) {
    try {
      Optional<Transaction> transaction = transactionRepository.findById(transactionId);
      return transaction.orElse(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void createNewTransaction(Transaction transaction) {
    try {
      transaction.setCreatedAt(ZonedDateTime.now());
      transaction.setUpdatedAt(ZonedDateTime.now());
      transactionRepository.save(transaction);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Transaction updateTransaction(Long transactionId, Transaction updateBody) {
    try {
      Optional<Transaction> origin = transactionRepository.findById(transactionId);
      if (origin.isPresent()) {
        Transaction oldTransaction = origin.get();
        oldTransaction.setCode(updateBody.getCode());
        oldTransaction.setType(updateBody.getType());
        oldTransaction.setMode(updateBody.getMode());
        oldTransaction.setStatus(updateBody.getStatus());
        oldTransaction.setContent(updateBody.getContent());
        oldTransaction.setUpdatedAt(ZonedDateTime.now());
        return transactionRepository.save(oldTransaction);
      } else {
        return transactionRepository.save(updateBody);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

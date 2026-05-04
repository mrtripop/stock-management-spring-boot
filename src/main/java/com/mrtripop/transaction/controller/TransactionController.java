package com.mrtripop.transaction.controller;

import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.transaction.models.Transaction;
import com.mrtripop.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @GetMapping
  public ResponseEntity<List<Transaction>> getTransaction(@Valid BaseQueryParams queryParams) {
    try {
      List<Transaction> transactions = this.transactionService.getTransactions(queryParams);
      return new ResponseEntity<>(transactions, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{userId}/users")
  public ResponseEntity<List<Transaction>> getTransactionByUserId(
      @PathVariable(name = "userId") Long userId) {
    try {
      List<Transaction> transactions = this.transactionService.getTransactionByUserId(userId);
      return new ResponseEntity<>(transactions, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{transactionId}")
  public ResponseEntity<Transaction> getTransactionById(
      @PathVariable(name = "transactionId") Long transactionId) {
    try {
      Transaction transaction = this.transactionService.getTransactionById(transactionId);
      if (transaction != null) {
        return new ResponseEntity<>(transaction, HttpStatus.OK);
      }
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{transactionId}")
  public ResponseEntity<Transaction> updateTransactionById(
      @PathVariable(name = "transactionId") Long transactionId,
      @RequestBody Transaction updateBody) {
    try {
      Transaction updateOrigin = transactionService.updateTransaction(transactionId, updateBody);
      return new ResponseEntity<>(updateOrigin, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

package com.mrtripop.order.controllers;

import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.order.models.Order;
import com.mrtripop.order.services.OrderService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/users/{userId}/orders")
  public ResponseEntity<List<Order>> retrieveOrders(
      @PathVariable(name = "userId") Long userId, BaseQueryParams queryParams) {
    try {
      List<Order> result = orderService.retrieveUserOrders(userId, queryParams);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/users/{userId}/orders/{orderId}")
  public ResponseEntity<Order> retrieveUserOrderById(
      @PathVariable(name = "userId", required = true) Long userId,
      @PathVariable(name = "orderId", required = true) Long orderId) {
    try {
      Order result = orderService.retrieveUserOrderById(userId, orderId);
      if (result == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/users/{userId}/orders")
  public ResponseEntity<Order> createNewOrder(
      @PathVariable(name = "userId") Long userId,
      @RequestParam(name = "addressId", defaultValue = "1") Long addressId,
      @RequestBody Order newOrder) {
    try {
      Order order = orderService.createUserOrder(userId, addressId, newOrder);
      if (order == null) return new ResponseEntity<>(HttpStatus.ACCEPTED);
      return new ResponseEntity<>(order, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/users/{userId}/orders/{orderId}")
  public ResponseEntity<Order> updateOrder(
      @PathVariable(name = "userId") Long userId,
      @PathVariable(name = "orderId") Long orderId,
      @RequestBody Order newOrder) {
    try {
      Order order = orderService.updateUserOrder(userId, orderId, newOrder);
      if (order == null) return new ResponseEntity<>(HttpStatus.ACCEPTED);
      return new ResponseEntity<>(order, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Transactional
  @DeleteMapping("/users/{userId}/orders")
  public ResponseEntity<Boolean> deleteUserOrder(@PathVariable(name = "userId") Long userId) {
    try {
      boolean order = orderService.deleteUserOrder(userId);
      if (!order) return new ResponseEntity<>(HttpStatus.ACCEPTED);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Controller: " + e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Transactional
  @DeleteMapping("/users/{userId}/orders/{orderId}")
  public ResponseEntity<?> deleteUserOrderById(
      @PathVariable(name = "userId") Long userId, @PathVariable(name = "orderId") Long orderId) {
    try {
      boolean order = orderService.deleteUserOrderById(userId, orderId);
      if (!order) return new ResponseEntity<>(HttpStatus.ACCEPTED);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.toString());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

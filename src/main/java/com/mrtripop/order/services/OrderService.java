package com.mrtripop.order.services;

import com.mrtripop.users.models.User;
import com.mrtripop.users.repositories.UserRepository;
import com.mrtripop.location.repositories.AddressRepository;
import com.mrtripop.model.QueryParams;
import com.mrtripop.order.models.Order;
import com.mrtripop.order.repositories.OrderRepository;
import com.mrtripop.transaction.service.TransactionService;
import com.mrtripop.util.DatabaseHelper;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final AddressRepository addressRepository;
  private final TransactionService transactionService;

  public OrderService(
      UserRepository userRepository,
      OrderRepository orderRepository,
      AddressRepository addressRepository,
      TransactionService transactionService) {
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.addressRepository = addressRepository;
    this.transactionService = transactionService;
  }

  public List<Order> retrieveUserOrders(Long userId, QueryParams queryParams) {
    try {
      boolean existUser = userRepository.existsById(userId);
      if (existUser) {
        Pageable pageSize = DatabaseHelper.initPageableWithSort(queryParams);
        Page<Order> orders = orderRepository.findByUserId(userId, pageSize);
        log.debug(orders.toString());
        return orders.stream().toList();
      } else {
        return List.of();
      }
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("RetrieveOrdersException", e.getCause());
    }
  }

  public Order retrieveUserOrderById(Long userId, Long orderId) {
    try {
      boolean existUser = userRepository.existsById(userId);
      if (!existUser) return null;
      Optional<Order> order = orderRepository.findByUserIdAndId(userId, orderId);
      return order.orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("RetrieveOrderByIdException", e.getCause());
    }
  }

  @Transactional
  public Order createUserOrder(Long userId, Long addressId, Order order) {
    try {
      // check user exist?
      Optional<User> user = userRepository.findById(userId);
      if (user.isEmpty()) return null;
      // set metadata of order
      //      Optional<Address> userAddress = addressRepository.findByUserIdAndId(userId,
      // addressId);
      //      if (userAddress.isEmpty()) return null;
      //      order.setCreatedAt(ZonedDateTime.now());
      //      order.setUpdatedAt(ZonedDateTime.now());
      //      order.setUser(user.get());
      //      order.setAddress(userAddress.get());
      //      // create order
      //      Order createOrder = orderRepository.save(order);
      //      // create transaction
      //      Transaction transaction = new Transaction();
      //      transaction.setCode("CREATE_ORDER_101");
      //      transaction.setType(1);
      //      transaction.setMode(1);
      //      transaction.setStatus(1);
      //      transaction.setContent(order.getContent());
      //      transaction.setUser(user.get());
      //      transaction.setOrder(createOrder);
      //      // create transaction
      //      transactionService.createNewTransaction(transaction);
      //      return createOrder;
      return null;
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("CreateOrderException", e.getCause());
    }
  }

  public Order updateUserOrder(Long userId, Long orderId, Order newOrder) {
    try {
      Optional<User> existedUser = userRepository.findById(userId);
      if (existedUser.isEmpty()) return null;

      Optional<Order> order = orderRepository.findByUserIdAndId(userId, orderId);
      if (order.isPresent()) {
        Order originalOrder = order.get();
        originalOrder.setType(newOrder.getType());
        originalOrder.setStatus(newOrder.getStatus());
        originalOrder.setSubTotal(newOrder.getSubTotal());
        originalOrder.setItemDiscount(newOrder.getItemDiscount());
        originalOrder.setTax(newOrder.getTax());
        originalOrder.setShipping(newOrder.getShipping());
        originalOrder.setTotal(newOrder.getTotal());
        originalOrder.setDiscount(newOrder.getDiscount());
        originalOrder.setGrandTotal(newOrder.getGrandTotal());
        originalOrder.setPromo(newOrder.getPromo());
        originalOrder.setContent(newOrder.getContent());
        originalOrder.setUpdatedAt(newOrder.getUpdatedAt());
        return orderRepository.save(originalOrder);
      } else {
        newOrder.setCreatedAt(ZonedDateTime.now());
        newOrder.setUser(existedUser.get());
        return orderRepository.save(newOrder);
      }
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("UpdateUserOrderByIdException", e.getCause());
    }
  }

  public boolean deleteUserOrder(Long userId) {
    try {
      boolean existedUser = userRepository.existsById(userId);
      if (!existedUser) return false;
      orderRepository.deleteByUserId(userId);
      return true;
    } catch (Exception e) {
      log.error("Service: " + e);
      throw new RuntimeException("DeleteUserOrdersException", e.getCause());
    }
  }

  public boolean deleteUserOrderById(Long userId, Long orderId) {
    try {
      boolean existedUser = userRepository.existsById(userId);
      if (!existedUser) return false;
      orderRepository.deleteByUserIdAndId(userId, orderId);
      return true;
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("DeleteUserOrderByIdException", e.getCause());
    }
  }
}

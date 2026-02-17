package com.mrtripop.users.controllers;

import com.mrtripop.users.models.User;
import com.mrtripop.users.services.UserService;
import com.mrtripop.constant.ErrorCode;
import com.mrtripop.constant.SuccessCode;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/test")
  public String test() {
    try {
      SuccessCode status = SuccessCode.USER2000_RETRIEVE_USERS_SUCCESS;
      WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).retrieveUserById(1L));
      return link.withRel("users-1").toString();

    } catch (Exception e) {
      return null;
    }
  }

  @GetMapping
  public ResponseEntity<Object> retrieveUsers(@Valid BaseQueryParams queryParams) {
    try {
      List<User> result = userService.retrieveUsers(queryParams);
      log.debug("Retrieve users: {}", result.toString());
      SuccessCode status = SuccessCode.USER2000_RETRIEVE_USERS_SUCCESS;
      return ResponseBody.builder()
          .code(status.getCode())
          .message(status.getMessage())
          .data(result)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot retrieve users: {}", e.getMessage());
      ErrorCode status = ErrorCode.USER5000_RETRIEVE_USERS_IS_FAILED;
      return ResponseBody.builder()
          .code(status.getCode())
          .message(status.getMessage())
          .error(e.getMessage())
          .build()
          .toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> retrieveUserById(@PathVariable Long id) {
    try {
      User user = userService.retrieveUserById(id);
      if (user == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      log.error(e.getMessage(), e.getCause());
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping
  public ResponseEntity<User> createNewUser(@RequestBody User user) {
    try {
      User result = userService.createNewUser(user);
      return ResponseEntity.created(URI.create("http://localhost:8080")).body(result);
    } catch (Exception e) {
      log.error(e.getMessage(), e.getCause());
      return ResponseEntity.status(500).body(null);
    }
  }

  @PutMapping("/{id}/general")
  public ResponseEntity<User> updateUserGeneralInfo(@PathVariable Long id, @RequestBody User user) {
    try {
      User updateUser = userService.updateUserGeneralInfo(id, user);
      return ResponseEntity.accepted().body(updateUser);
    } catch (Exception e) {
      log.error(e.getMessage(), e.getCause());
      return ResponseEntity.status(500).body(null);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
    try {
      boolean status = userService.deleteUserById(id);
      if (status) {
        return ResponseEntity.accepted().body("Delete user id: " + id + " success!");
      }
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error(e.getMessage(), e.getCause());
      return ResponseEntity.status(500).body(null);
    }
  }
}

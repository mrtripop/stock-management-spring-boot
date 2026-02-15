package com.mrtripop.users.utils;

import com.mrtripop.users.models.User;
import java.time.ZonedDateTime;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecificationHelper {

  private UserSpecificationHelper() {}

  public static Specification<User> lastLogin(ZonedDateTime start) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.between(root.get("last_login"), start, start);
  }

  public static Specification<User> todayRegister(ZonedDateTime today) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("registered_at"), today);
  }
}

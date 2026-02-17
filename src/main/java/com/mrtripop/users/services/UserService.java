package com.mrtripop.users.services;

import com.mrtripop.users.models.User;
import com.mrtripop.users.repositories.UserRepository;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.util.DatabaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> retrieveUsers(BaseQueryParams queryParams) {
    try {
      Pageable pageSize = DatabaseHelper.initPageableWithSort(queryParams);
      Page<User> pageUser = userRepository.findAll(pageSize);
      log.debug(pageUser.toString());
      return pageUser.stream().toList();
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException("RetrieveUsersException", e.getCause());
    }
  }

  public String getDay(int year, int month, int day){
    Calendar calendar = new GregorianCalendar();
    calendar.set(year, month, day);
    return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
  }

  public User retrieveUserById(Long id) {
    try {
      Optional<User> user = this.userRepository.findById(id);
      return user.orElse(null);
    } catch (Exception e) {
      throw new RuntimeException("RetrieveUserByIdException", e.getCause());
    }
  }

  public User createNewUser(User user) {
    try {
      user.setRegisteredAt(ZonedDateTime.now());
      user.setLastLogin(ZonedDateTime.now());
      return userRepository.save(user);
    } catch (Exception e) {
      throw new RuntimeException("CreateNewUserException", e.getCause());
    }
  }

  public User updateUserGeneralInfo(Long id, User userUpdate) {
    try {
      Optional<User> user = userRepository.findById(id);
      if (user.isPresent()) {
        User originalUser = user.get();
        originalUser.setRoleId(userUpdate.getRoleId());
        originalUser.setFirstName(userUpdate.getFirstName());
        originalUser.setLastName(userUpdate.getLastName());
        originalUser.setUsername(userUpdate.getUsername());
        originalUser.setMobile(userUpdate.getMobile());
        originalUser.setEmail(userUpdate.getEmail());
        originalUser.setIntro(userUpdate.getIntro());
        originalUser.setProfile(userUpdate.getProfile());
        userRepository.save(originalUser);
        return originalUser;
      }
      userRepository.save(userUpdate);
      return userUpdate;
    } catch (Exception e) {
      throw new RuntimeException("UpdateUserById", e.getCause());
    }
  }

  public boolean deleteUserById(Long id) {
    try {
      Optional<User> originalUser = userRepository.findById(id);
      if (originalUser.isPresent()) {
        userRepository.delete(originalUser.get());
        return true;
      }
      return false;
    } catch (Exception e) {
      String error = ExceptionUtils.getStackTrace(e);
      log.error(error);
      throw new RuntimeException("DeleteUserById", e.getCause());
    }
  }
}

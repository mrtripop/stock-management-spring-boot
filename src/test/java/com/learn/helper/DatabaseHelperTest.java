package com.learn.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class DatabaseHelperTest {

  @Test
  @DisplayName("Test update page request with order by desc")
  void initSortOrder_ReturnASC_WhenOrderByAscString() {
    // arrange
    PageRequest pageRequest = PageRequest.of(10, 10);
    String orderBy = "asc";
    // action
    PageRequest updatePageRequest = DatabaseHelper.initSortOrder(pageRequest, orderBy);
    // assert
    assertTrue(updatePageRequest.getSort().isSorted());
  }

  @Test
  @DisplayName("Test create page request with page and size is less than zero")
  void testInitPageable_ReturnDefaultPageRequest_WhenPageAndSizeIsLessThanZero() {
    // arrange
    Integer page = -10;
    Integer size = -10;
    // action
    PageRequest pageSize = DatabaseHelper.initPageable(page, size);
    // assert
    assertEquals(0, pageSize.getPageNumber());
    assertEquals(10, pageSize.getPageSize());
  }

  @Test
  @DisplayName("Test create page request with size is more than zero")
  void testInitPageable_ReturnPageRequestWithSize_WhenSizeIsMoreThanZero() {
    // arrange
    Integer page = -10;
    Integer size = 20;
    // action
    PageRequest pageSize = DatabaseHelper.initPageable(page, size);
    // assert
    assertEquals(0, pageSize.getPageNumber());
    assertEquals(20, pageSize.getPageSize());
  }

  @Test
  @DisplayName("Test create page request with size and page is more than zero")
  void testInitPageable_ReturnPageRequestWithPageAndSize_WhenPageAndSizeIsMoreThanZero() {
    // arrange
    Integer page = 1;
    Integer size = 20;
    // action
    PageRequest pageSize = DatabaseHelper.initPageable(page, size);
    // assert
    assertEquals(0, pageSize.getPageNumber());
    assertEquals(20, pageSize.getPageSize());
  }
}

package com.mrtripop.learn;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BigDecimalTest {

  @Test
  @DisplayName("Verify decimal number precision when minus operation with double number")
  void verify_decimal_number_precision_when_minus_operation_with_double_number() {
    double number1 = 0.4;
    double number2 = 0.3;
    // action
    double result = number1 - number2;
    // assert
    assertNotEquals(0.1, result);
  }

  @Test
  @DisplayName("Verify decimal number precision when minus operation with float number")
  void verify_decimal_number_precision_when_minus_operation_with_float_number() {
    float number1 = 0.4f;
    float number2 = 0.3f;
    // action
    float result = number1 - number2;
    // assert
    assertNotEquals(0.1, result);
  }

  @Test
  @DisplayName(
      "Verify decimal number precision when minus operation with big decimal number from string")
  void verify_decimal_number_precision_when_minus_operation_with_big_decimal_number_from_string() {
    BigDecimal number1 = new BigDecimal("0.4");
    BigDecimal number2 = new BigDecimal("0.3");
    // action
    BigDecimal result = number1.subtract(number2);
    // assert
    assertEquals(0.1, result.doubleValue());
  }

  @Test
  @DisplayName(
      "Verify decimal number precision when minus operation with big decimal number from double")
  void verify_decimal_number_precision_when_minus_operation_with_big_decimal_number_from_double() {
    BigDecimal number1 = BigDecimal.valueOf(0.4);
    BigDecimal number2 = BigDecimal.valueOf(0.3);
    // action
    BigDecimal result = number1.subtract(number2);
    // assert
    assertEquals(0.1, result.doubleValue());
  }


  @Test
  @DisplayName("Remove useless zero after calculation with two number")
  void remove_useless_zero_after_calculation_with_two_number(){

    BigDecimal number1 = BigDecimal.valueOf(0.5);
    BigDecimal number2 = BigDecimal.valueOf(0.2);
    // action
    BigDecimal result = number1.multiply(number2);
    // assert
    assertEquals("0.1", result.toString());
  }
}

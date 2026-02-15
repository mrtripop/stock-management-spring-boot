package com.mrtripop.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PocBigDecimalFeature {

  @Test
  @DisplayName("[POC] Verify assumption about PI in computer")
  void poc_verify_assumption_about_PI_in_computer() {
    BigDecimal n1 = BigDecimal.valueOf(22);
    BigDecimal n2 = BigDecimal.valueOf(7);

    BigDecimal result = n1.divide(n2, new MathContext(3, RoundingMode.DOWN));

    assertEquals(3.14, result.doubleValue());
  }

  @Test
  @DisplayName("[POC] Verify assumption about scale in decimal and rounding mode")
  void poc_verify_assumption_about_scale_in_decimal_and_rounding_mode() {
    BigDecimal number =
        BigDecimal.valueOf(
            1.5999842313164864354987949849431321688978979794564564656119818916866846484686484613534846);

    BigDecimal result = number.setScale(10, RoundingMode.DOWN);

    assertEquals(1.5999842313, result.doubleValue());
  }

  @Test
  @DisplayName("[POC] Verify assumption about strip trailing zeros")
  void poc_verify_assumption_about_strip_trailing_zeros() {
    BigDecimal n1 = BigDecimal.valueOf(0.999999);
    BigDecimal n2 = BigDecimal.valueOf(0.000001);

    BigDecimal result = n1.add(n2).stripTrailingZeros();

    assertEquals(new BigDecimal("1"), result);
  }
}

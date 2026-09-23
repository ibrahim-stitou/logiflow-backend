package com.logiflow.tms.shared.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class JpaTupleAgregatTest {

  @Test
  void normaliserLigne_returnsFlatTupleWhenAlreadyFlat() {
    Object[] flat = {3L, 120.5, BigDecimal.valueOf(99.9)};
    assertArrayEquals(flat, JpaTupleAgregat.normaliserLigne(flat));
  }

  @Test
  void normaliserLigne_unwrapsSingleNestedTuple() {
    Object[] flat = {3L, 120.5, BigDecimal.valueOf(99.9)};
    Object[] wrapped = {flat};
    assertArrayEquals(flat, JpaTupleAgregat.normaliserLigne(wrapped));
  }

  @Test
  void versEntier_acceptsLongAndInteger() {
    assertEquals(5L, JpaTupleAgregat.versEntier(5L));
    assertEquals(5L, JpaTupleAgregat.versEntier(5));
  }
}

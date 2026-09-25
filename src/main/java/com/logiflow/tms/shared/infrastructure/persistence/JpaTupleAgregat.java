package com.logiflow.tms.shared.infrastructure.persistence;

import java.math.BigDecimal;

/** Normalises Hibernate aggregate query rows (single tuple vs nested wrapper). */
public final class JpaTupleAgregat {

  private JpaTupleAgregat() {}

  public static Object[] normaliserLigne(Object raw) {
    if (raw == null) {
      return null;
    }
    if (!(raw instanceof Object[] line)) {
      throw new IllegalStateException(
          "Ligne agrégée inattendue: " + raw.getClass().getName());
    }
    if (line.length == 1 && line[0] instanceof Object[] nested) {
      return nested;
    }
    return line;
  }

  public static long versEntier(Object value) {
    if (value == null) {
      return 0L;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    throw new IllegalStateException("Entier agrégé inattendu: " + value.getClass().getName());
  }

  public static double versReel(Object value) {
    if (value == null) {
      return 0.0;
    }
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    throw new IllegalStateException("Réel agrégé inattendu: " + value.getClass().getName());
  }

  public static BigDecimal versBigDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    throw new IllegalStateException("Montant agrégé inattendu: " + value.getClass().getName());
  }
}

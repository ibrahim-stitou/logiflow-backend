package com.logiflow.tms.shared.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/** Montant monétaire associé à une devise. Les opérations refusent le mélange de devises. */
public record Money(BigDecimal montant, Currency devise) {

  public Money {
    Objects.requireNonNull(montant, "Le montant est obligatoire");
    Objects.requireNonNull(devise, "La devise est obligatoire");
    int decimales = devise.getDefaultFractionDigits() < 0 ? 2 : devise.getDefaultFractionDigits();
    montant = montant.setScale(decimales, RoundingMode.HALF_UP);
  }

  public static Money zero(Currency devise) {
    return new Money(BigDecimal.ZERO, devise);
  }

  public Money plus(Money autre) {
    verifierMemeDevise(autre);
    return new Money(montant.add(autre.montant), devise);
  }

  public Money moins(Money autre) {
    verifierMemeDevise(autre);
    return new Money(montant.subtract(autre.montant), devise);
  }

  public Money multiplie(BigDecimal facteur) {
    Objects.requireNonNull(facteur, "Le facteur est obligatoire");
    return new Money(montant.multiply(facteur), devise);
  }

  private void verifierMemeDevise(Money autre) {
    Objects.requireNonNull(autre, "Le montant à comparer est obligatoire");
    if (!devise.equals(autre.devise)) {
      throw new IllegalArgumentException(
          "Impossible de combiner des montants de devises différentes (%s / %s)"
              .formatted(devise, autre.devise));
    }
  }
}

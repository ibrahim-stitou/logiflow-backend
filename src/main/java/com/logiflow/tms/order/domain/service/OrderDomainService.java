package com.logiflow.tms.order.domain.service;

import java.time.LocalDate;
import java.util.Objects;

/** Règles métier du module {@code order} qui n'appartiennent à aucune entité en particulier. */
public class OrderDomainService {

  /** Une commande ne peut porter une date souhaitée déjà passée. */
  public void verifierDateSouhaitee(LocalDate dateSouhaitee, LocalDate aujourdHui) {
    Objects.requireNonNull(dateSouhaitee, "La date souhaitée est obligatoire");
    Objects.requireNonNull(aujourdHui, "La date du jour est obligatoire");
    if (dateSouhaitee.isBefore(aujourdHui)) {
      throw new IllegalArgumentException(
          "La date souhaitée ne peut pas être antérieure à aujourd'hui");
    }
  }
}

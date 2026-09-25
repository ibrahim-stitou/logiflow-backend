package com.logiflow.tms.maintenance.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Coûts de maintenance d'une période (ordres de travail terminés, montants HT) et sinistralité
 * (coût net des sinistres survenus = réparations − indemnités).
 */
public record CoutsMaintenanceSummary(
    LocalDate debut,
    LocalDate fin,
    BigDecimal totalHt,
    BigDecimal totalTtc,
    long nombreOrdres,
    BigDecimal budgetEstime,
    List<Poste> parType,
    List<Poste> parNature,
    List<Poste> parEngin,
    long nombreSinistres,
    BigDecimal coutNetSinistres,
    BigDecimal indemnitesPercues) {

  /** Poste de répartition ({@code libelle} : immatriculation pour un engin). */
  public record Poste(String cle, String libelle, BigDecimal totalHt, long nombre) {}
}

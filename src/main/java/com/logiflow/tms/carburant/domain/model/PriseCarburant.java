package com.logiflow.tms.carburant.domain.model;

import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Ticket carburant réel rattaché à un voyage. */
public final class PriseCarburant {

  private final UUID id;
  private final UUID voyageId;
  private final UUID vehiculeId;
  private final UUID remorqueId;
  private UUID stationId;
  private TypeCarburant typeCarburant;
  private double litrage;
  private BigDecimal montantTtc;
  private Instant datePrise;
  private StatutPrise statut;

  private PriseCarburant(
      UUID id,
      UUID voyageId,
      UUID vehiculeId,
      UUID remorqueId,
      UUID stationId,
      TypeCarburant typeCarburant,
      double litrage,
      BigDecimal montantTtc,
      Instant datePrise,
      StatutPrise statut) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la prise est obligatoire");
    this.voyageId = Objects.requireNonNull(voyageId, "Le voyage est obligatoire");
    verifierXorEngin(vehiculeId, remorqueId);
    this.vehiculeId = vehiculeId;
    this.remorqueId = remorqueId;
    this.stationId = Objects.requireNonNull(stationId, "La station est obligatoire");
    this.typeCarburant =
        Objects.requireNonNull(typeCarburant, "Le type de carburant est obligatoire");
    this.litrage = validerLitrage(litrage);
    this.montantTtc = validerMontant(montantTtc);
    this.datePrise = Objects.requireNonNull(datePrise, "La date de prise est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
  }

  public static PriseCarburant creer(
      UUID id,
      UUID voyageId,
      UUID vehiculeId,
      UUID remorqueId,
      UUID stationId,
      TypeCarburant typeCarburant,
      double litrage,
      BigDecimal montantTtc,
      Instant datePrise) {
    return new PriseCarburant(
        id,
        voyageId,
        vehiculeId,
        remorqueId,
        stationId,
        typeCarburant,
        litrage,
        montantTtc,
        datePrise,
        StatutPrise.BROUILLON);
  }

  public static PriseCarburant reconstituer(
      UUID id,
      UUID voyageId,
      UUID vehiculeId,
      UUID remorqueId,
      UUID stationId,
      TypeCarburant typeCarburant,
      double litrage,
      BigDecimal montantTtc,
      Instant datePrise,
      StatutPrise statut) {
    return new PriseCarburant(
        id,
        voyageId,
        vehiculeId,
        remorqueId,
        stationId,
        typeCarburant,
        litrage,
        montantTtc,
        datePrise,
        statut);
  }

  public void modifier(
      UUID stationId, TypeCarburant typeCarburant, double litrage, BigDecimal montantTtc) {
    verifierModifiable();
    this.stationId = Objects.requireNonNull(stationId, "La station est obligatoire");
    this.typeCarburant =
        Objects.requireNonNull(typeCarburant, "Le type de carburant est obligatoire");
    this.litrage = validerLitrage(litrage);
    this.montantTtc = validerMontant(montantTtc);
  }

  public void valider() {
    verifierModifiable();
    this.statut = StatutPrise.VALIDEE;
  }

  public boolean estModifiable() {
    return statut == StatutPrise.BROUILLON;
  }

  private void verifierModifiable() {
    if (!estModifiable()) {
      throw new BusinessException("Une prise validée ne peut plus être modifiée");
    }
  }

  private static void verifierXorEngin(UUID vehiculeId, UUID remorqueId) {
    boolean vehicule = vehiculeId != null;
    boolean remorque = remorqueId != null;
    if (vehicule == remorque) {
      throw new IllegalArgumentException(
          "Une prise doit cibler le véhicule ou la remorque du voyage, pas les deux ni aucun");
    }
  }

  private static double validerLitrage(double litrage) {
    if (litrage <= 0) {
      throw new IllegalArgumentException("Le litrage doit être strictement positif");
    }
    return litrage;
  }

  private static BigDecimal validerMontant(BigDecimal montantTtc) {
    Objects.requireNonNull(montantTtc, "Le montant TTC est obligatoire");
    if (montantTtc.signum() <= 0) {
      throw new IllegalArgumentException("Le montant TTC doit être strictement positif");
    }
    return montantTtc;
  }

  public UUID id() {
    return id;
  }

  public UUID voyageId() {
    return voyageId;
  }

  public UUID vehiculeId() {
    return vehiculeId;
  }

  public UUID remorqueId() {
    return remorqueId;
  }

  public UUID stationId() {
    return stationId;
  }

  public TypeCarburant typeCarburant() {
    return typeCarburant;
  }

  public double litrage() {
    return litrage;
  }

  public BigDecimal montantTtc() {
    return montantTtc;
  }

  public Instant datePrise() {
    return datePrise;
  }

  public StatutPrise statut() {
    return statut;
  }
}

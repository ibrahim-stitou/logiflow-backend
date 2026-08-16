package com.logiflow.tms.maintenance.domain.model;

import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Ordre de travail : intervention planifiée ou réalisée sur un véhicule. */
public final class OrdreTravail {

  private static final Map<StatutOT, Set<StatutOT>> TRANSITIONS_AUTORISEES =
      Map.of(
          StatutOT.PLANIFIE, Set.of(StatutOT.EN_COURS, StatutOT.ANNULE),
          StatutOT.EN_COURS, Set.of(StatutOT.TERMINE, StatutOT.ANNULE),
          StatutOT.TERMINE, Set.of(),
          StatutOT.ANNULE, Set.of());

  private final UUID id;
  private final UUID vehiculeId;
  private final TypeIntervention type;
  private StatutOT statut;
  private LocalDateTime datePlanifiee;
  private int dureeReelleMin;
  private Money cout;

  private OrdreTravail(
      UUID id,
      UUID vehiculeId,
      TypeIntervention type,
      StatutOT statut,
      LocalDateTime datePlanifiee,
      int dureeReelleMin,
      Money cout) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'ordre de travail est obligatoire");
    this.vehiculeId = Objects.requireNonNull(vehiculeId, "Le véhicule est obligatoire");
    this.type = Objects.requireNonNull(type, "Le type d'intervention est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.datePlanifiee = Objects.requireNonNull(datePlanifiee, "La date planifiée est obligatoire");
    if (dureeReelleMin < 0) {
      throw new IllegalArgumentException("La durée réelle ne peut pas être négative");
    }
    this.dureeReelleMin = dureeReelleMin;
    this.cout = Objects.requireNonNull(cout, "Le coût est obligatoire");
  }

  public static OrdreTravail creer(
      UUID id, UUID vehiculeId, TypeIntervention type, LocalDateTime datePlanifiee, Money cout) {
    return new OrdreTravail(id, vehiculeId, type, StatutOT.PLANIFIE, datePlanifiee, 0, cout);
  }

  public static OrdreTravail reconstituer(
      UUID id,
      UUID vehiculeId,
      TypeIntervention type,
      StatutOT statut,
      LocalDateTime datePlanifiee,
      int dureeReelleMin,
      Money cout) {
    return new OrdreTravail(id, vehiculeId, type, statut, datePlanifiee, dureeReelleMin, cout);
  }

  public void changerStatut(StatutOT nouveauStatut) {
    Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
    if (!TRANSITIONS_AUTORISEES.getOrDefault(statut, Set.of()).contains(nouveauStatut)) {
      throw new BusinessException(
          "Transition de statut invalide pour l'ordre de travail : %s -> %s"
              .formatted(statut, nouveauStatut));
    }
    this.statut = nouveauStatut;
  }

  public void cloturer(int dureeReelleMin, Money coutReel) {
    changerStatut(StatutOT.TERMINE);
    if (dureeReelleMin < 0) {
      throw new IllegalArgumentException("La durée réelle ne peut pas être négative");
    }
    this.dureeReelleMin = dureeReelleMin;
    this.cout = Objects.requireNonNull(coutReel, "Le coût réel est obligatoire");
  }

  public UUID id() {
    return id;
  }

  public UUID vehiculeId() {
    return vehiculeId;
  }

  public TypeIntervention type() {
    return type;
  }

  public StatutOT statut() {
    return statut;
  }

  public LocalDateTime datePlanifiee() {
    return datePlanifiee;
  }

  public int dureeReelleMin() {
    return dureeReelleMin;
  }

  public Money cout() {
    return cout;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrdreTravail that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

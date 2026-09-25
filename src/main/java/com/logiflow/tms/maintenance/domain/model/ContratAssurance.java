package com.logiflow.tms.maintenance.domain.model;

import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Contrat d'assurance : toute la flotte (FLOTTE) ou des engins désignés (ENGIN). Fournit assureur
 * et franchise aux sinistres des engins couverts.
 */
public final class ContratAssurance {

  /** Conditions du contrat, modifiables. */
  public record Conditions(
      UUID assureurId,
      String numeroPolice,
      TypeContrat type,
      Set<Garantie> garanties,
      Money franchise,
      Money primeAnnuelle,
      LocalDate dateEffet,
      LocalDate dateEcheance,
      List<EnginRef> engins,
      boolean actif) {

    public Conditions {
      Objects.requireNonNull(assureurId, "L'assureur est obligatoire");
      if (numeroPolice == null || numeroPolice.isBlank()) {
        throw new IllegalArgumentException("Le numéro de police est obligatoire");
      }
      numeroPolice = numeroPolice.strip();
      Objects.requireNonNull(type, "Le type de contrat est obligatoire");
      garanties = garanties == null ? Set.of() : Set.copyOf(garanties);
      if (garanties.isEmpty()) {
        throw new IllegalArgumentException("Un contrat couvre au moins une garantie");
      }
      Objects.requireNonNull(dateEffet, "La date d'effet est obligatoire");
      Objects.requireNonNull(dateEcheance, "La date d'échéance est obligatoire");
      if (!dateEcheance.isAfter(dateEffet)) {
        throw new IllegalArgumentException("L'échéance doit suivre la date d'effet");
      }
      engins = engins == null ? List.of() : List.copyOf(engins);
      if (type == TypeContrat.ENGIN && engins.isEmpty()) {
        throw new IllegalArgumentException("Un contrat par engin doit désigner au moins un engin");
      }
      if (type == TypeContrat.FLOTTE) {
        engins = List.of();
      }
    }
  }

  private final UUID id;
  private Conditions conditions;

  private ContratAssurance(UUID id, Conditions conditions) {
    this.id = Objects.requireNonNull(id, "L'identifiant du contrat est obligatoire");
    this.conditions = Objects.requireNonNull(conditions, "Les conditions sont obligatoires");
  }

  public static ContratAssurance creer(UUID id, Conditions conditions) {
    return new ContratAssurance(id, conditions);
  }

  public static ContratAssurance reconstituer(UUID id, Conditions conditions) {
    return new ContratAssurance(id, conditions);
  }

  public void modifier(Conditions nouvelles) {
    this.conditions = Objects.requireNonNull(nouvelles, "Les conditions sont obligatoires");
  }

  /** Le contrat est-il en vigueur à cette date et couvre-t-il cet engin ? */
  public boolean couvre(EnginRef engin, LocalDate date) {
    return conditions.actif()
        && !date.isBefore(conditions.dateEffet())
        && !date.isAfter(conditions.dateEcheance())
        && (conditions.type() == TypeContrat.FLOTTE || conditions.engins().contains(engin));
  }

  /** Un contrat dédié à l'engin prime sur un contrat de flotte. */
  public int specificite() {
    return conditions.type() == TypeContrat.ENGIN ? 1 : 0;
  }

  public UUID id() {
    return id;
  }

  public Conditions conditions() {
    return conditions;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof ContratAssurance autre && id.equals(autre.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

package com.logiflow.tms.maintenance.domain.model;

import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.shared.domain.DeviseApplication;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Ordre de travail : intervention planifiée puis réalisée sur un engin (véhicule ou remorque), en
 * atelier interne ou chez un prestataire, avec ses lignes de coût.
 *
 * <p>Cycle : PLANIFIE → EN_COURS ⇄ EN_ATTENTE_PIECES → TERMINE (par {@link #cloturer}), ou ANNULE
 * tant qu'il n'est pas terminé. Un OT terminé ou annulé n'est plus modifiable.
 */
public final class OrdreTravail {

  /** Devise des montants de maintenance : celle de l'application. */
  public static final Currency DEVISE = DeviseApplication.PAR_DEFAUT;

  private static final Map<StatutOT, Set<StatutOT>> TRANSITIONS =
      Map.of(
          StatutOT.PLANIFIE, Set.of(StatutOT.EN_COURS, StatutOT.ANNULE),
          StatutOT.EN_COURS, Set.of(StatutOT.EN_ATTENTE_PIECES, StatutOT.ANNULE),
          StatutOT.EN_ATTENTE_PIECES, Set.of(StatutOT.EN_COURS, StatutOT.ANNULE),
          StatutOT.TERMINE, Set.of(),
          StatutOT.ANNULE, Set.of());

  /** Données descriptives modifiables tant que l'OT n'est pas terminé. */
  public record DetailsOT(
      TypeIntervention type,
      NatureIntervention nature,
      PrioriteOT priorite,
      String titre,
      String description,
      UUID prestataireId,
      LocalDateTime debutPlanifie,
      LocalDateTime finPlanifiee,
      boolean immobilisation,
      Money budgetEstime) {

    public DetailsOT {
      Objects.requireNonNull(type, "Le type d'intervention est obligatoire");
      Objects.requireNonNull(nature, "La nature de l'intervention est obligatoire");
      priorite = priorite == null ? PrioriteOT.NORMALE : priorite;
      if (titre == null || titre.isBlank()) {
        throw new IllegalArgumentException("Le titre de l'ordre de travail est obligatoire");
      }
      titre = titre.strip();
      Objects.requireNonNull(debutPlanifie, "Le début planifié est obligatoire");
      if (finPlanifiee != null && finPlanifiee.isBefore(debutPlanifie)) {
        throw new IllegalArgumentException("La fin planifiée doit suivre le début planifié");
      }
      if (budgetEstime != null && budgetEstime.montant().signum() < 0) {
        throw new IllegalArgumentException("Le budget estimé ne peut pas être négatif");
      }
    }
  }

  /** Données de réalisation, complétées au démarrage puis à la clôture. */
  public record Realisation(
      LocalDateTime debutReel,
      LocalDateTime finReelle,
      Integer kilometrage,
      Integer heures,
      String diagnostic,
      String travauxRealises,
      String intervenant,
      String numeroFacture,
      LocalDate dateFacture) {

    public static Realisation vide() {
      return new Realisation(null, null, null, null, null, null, null, null, null);
    }

    Realisation avecDebut(LocalDateTime debut) {
      return new Realisation(
          debut,
          finReelle,
          kilometrage,
          heures,
          diagnostic,
          travauxRealises,
          intervenant,
          numeroFacture,
          dateFacture);
    }
  }

  /** Données saisies à la clôture. */
  public record Cloture(
      LocalDateTime finReelle,
      Integer kilometrage,
      Integer heures,
      String diagnostic,
      String travauxRealises,
      String intervenant,
      String numeroFacture,
      LocalDate dateFacture) {}

  private final UUID id;
  private final Reference reference;
  private final EnginRef engin;
  private final OrigineOT origine;
  private final UUID planId;
  private final UUID sinistreId;
  private DetailsOT details;
  private StatutOT statut;
  private List<LigneCout> lignes;
  private Realisation realisation;

  private OrdreTravail(
      UUID id,
      Reference reference,
      EnginRef engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      DetailsOT details,
      StatutOT statut,
      List<LigneCout> lignes,
      Realisation realisation) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'ordre de travail est obligatoire");
    this.reference = Objects.requireNonNull(reference, "La référence est obligatoire");
    this.engin = Objects.requireNonNull(engin, "L'engin est obligatoire");
    this.origine = Objects.requireNonNull(origine, "L'origine est obligatoire");
    if (origine == OrigineOT.PLAN_ENTRETIEN && planId == null) {
      throw new IllegalArgumentException(
          "Un OT issu d'un plan d'entretien doit référencer le plan");
    }
    if (origine == OrigineOT.SINISTRE && sinistreId == null) {
      throw new IllegalArgumentException("Un OT issu d'un sinistre doit référencer le sinistre");
    }
    this.planId = planId;
    this.sinistreId = sinistreId;
    this.details = Objects.requireNonNull(details, "Les détails sont obligatoires");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.lignes = List.copyOf(Objects.requireNonNull(lignes, "Les lignes sont obligatoires"));
    this.realisation = Objects.requireNonNull(realisation, "La réalisation est obligatoire");
  }

  public static OrdreTravail creer(
      UUID id,
      Reference reference,
      EnginRef engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      DetailsOT details) {
    return new OrdreTravail(
        id,
        reference,
        engin,
        origine,
        planId,
        sinistreId,
        details,
        StatutOT.PLANIFIE,
        List.of(),
        Realisation.vide());
  }

  public static OrdreTravail reconstituer(
      UUID id,
      Reference reference,
      EnginRef engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      DetailsOT details,
      StatutOT statut,
      List<LigneCout> lignes,
      Realisation realisation) {
    return new OrdreTravail(
        id, reference, engin, origine, planId, sinistreId, details, statut, lignes, realisation);
  }

  // ─── Comportements ─────────────────────────────────────────────────────

  public void modifier(DetailsOT nouveauxDetails) {
    verifierModifiable();
    this.details = Objects.requireNonNull(nouveauxDetails, "Les détails sont obligatoires");
  }

  public void remplacerLignes(List<LigneCout> nouvellesLignes) {
    verifierModifiable();
    this.lignes =
        List.copyOf(Objects.requireNonNull(nouvellesLignes, "Les lignes sont obligatoires"));
  }

  /**
   * Transitions simples (démarrage, attente de pièces, reprise, annulation). La fin se fait par
   * {@link #cloturer}.
   */
  public void changerStatut(StatutOT nouveau, LocalDateTime maintenant) {
    Objects.requireNonNull(nouveau, "Le nouveau statut est obligatoire");
    if (nouveau == StatutOT.TERMINE) {
      throw new BusinessException("Un ordre de travail se termine par sa clôture");
    }
    if (!TRANSITIONS.getOrDefault(statut, Set.of()).contains(nouveau)) {
      throw new BusinessException(
          "Transition de statut invalide pour l'ordre de travail : %s -> %s"
              .formatted(statut, nouveau));
    }
    if (nouveau == StatutOT.EN_COURS && realisation.debutReel() == null) {
      realisation = realisation.avecDebut(maintenant);
    }
    this.statut = nouveau;
  }

  public void cloturer(Cloture cloture) {
    Objects.requireNonNull(cloture, "Les données de clôture sont obligatoires");
    if (statut != StatutOT.EN_COURS && statut != StatutOT.EN_ATTENTE_PIECES) {
      throw new BusinessException("Seul un ordre de travail en cours peut être clôturé");
    }
    if (cloture.finReelle() == null) {
      throw new BusinessException("La date de fin réelle est obligatoire à la clôture");
    }
    if (realisation.debutReel() != null && cloture.finReelle().isBefore(realisation.debutReel())) {
      throw new BusinessException("La fin réelle ne peut pas précéder le début réel");
    }
    if (engin.type() == TypeEngin.VEHICULE && cloture.kilometrage() == null) {
      throw new BusinessException(
          "Le kilométrage relevé est obligatoire pour clôturer l'OT d'un véhicule");
    }
    if (cloture.kilometrage() != null && cloture.kilometrage() < 0
        || cloture.heures() != null && cloture.heures() < 0) {
      throw new BusinessException("Les compteurs relevés ne peuvent pas être négatifs");
    }
    if (lignes.isEmpty()) {
      throw new BusinessException("Au moins une ligne de coût est requise pour clôturer l'OT");
    }
    this.realisation =
        new Realisation(
            realisation.debutReel() != null ? realisation.debutReel() : details.debutPlanifie(),
            cloture.finReelle(),
            cloture.kilometrage(),
            cloture.heures(),
            cloture.diagnostic(),
            cloture.travauxRealises(),
            cloture.intervenant(),
            cloture.numeroFacture(),
            cloture.dateFacture());
    this.statut = StatutOT.TERMINE;
  }

  private void verifierModifiable() {
    if (statut == StatutOT.TERMINE || statut == StatutOT.ANNULE) {
      throw new BusinessException("Un ordre de travail terminé ou annulé n'est plus modifiable");
    }
  }

  // ─── Calculs ───────────────────────────────────────────────────────────

  public Money totalHt() {
    return lignes.stream().map(LigneCout::totalHt).reduce(Money.zero(DEVISE), Money::plus);
  }

  public Money totalTva() {
    return lignes.stream().map(LigneCout::tva).reduce(Money.zero(DEVISE), Money::plus);
  }

  public Money totalTtc() {
    return totalHt().plus(totalTva());
  }

  /** Durée d'immobilisation réelle en heures (null tant que l'OT n'est pas démarré et terminé). */
  public Long immobilisationHeures() {
    if (realisation.debutReel() == null || realisation.finReelle() == null) {
      return null;
    }
    return Duration.between(realisation.debutReel(), realisation.finReelle()).toHours();
  }

  /** L'OT mobilise-t-il l'engin (atelier) : planifié ou en cours, avec immobilisation. */
  public boolean mobiliseEngin() {
    return details.immobilisation()
        && (statut == StatutOT.PLANIFIE
            || statut == StatutOT.EN_COURS
            || statut == StatutOT.EN_ATTENTE_PIECES);
  }

  public boolean estOuvert() {
    return statut != StatutOT.TERMINE && statut != StatutOT.ANNULE;
  }

  // ─── Accesseurs ────────────────────────────────────────────────────────

  public UUID id() {
    return id;
  }

  public Reference reference() {
    return reference;
  }

  public EnginRef engin() {
    return engin;
  }

  public OrigineOT origine() {
    return origine;
  }

  public UUID planId() {
    return planId;
  }

  public UUID sinistreId() {
    return sinistreId;
  }

  public DetailsOT details() {
    return details;
  }

  public StatutOT statut() {
    return statut;
  }

  public List<LigneCout> lignes() {
    return lignes;
  }

  public Realisation realisation() {
    return realisation;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof OrdreTravail autre && id.equals(autre.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

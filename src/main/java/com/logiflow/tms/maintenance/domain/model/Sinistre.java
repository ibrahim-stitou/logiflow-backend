package com.logiflow.tms.maintenance.domain.model;

import com.logiflow.tms.maintenance.domain.vo.Tiers;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Sinistre touchant un véhicule et/ou une remorque : circonstances, tiers, suivi assurance
 * (déclaration, expertise, indemnisation) et coût net une fois les réparations réalisées.
 */
public final class Sinistre {

  /** Délai contractuel usuel de déclaration à l'assureur, en jours ouvrés. */
  public static final int DELAI_DECLARATION_JOURS_OUVRES = 5;

  private static final Map<StatutSinistre, Set<StatutSinistre>> TRANSITIONS =
      Map.of(
          StatutSinistre.DECLARE,
          Set.of(
              StatutSinistre.DECLARE_ASSUREUR,
              StatutSinistre.EN_REPARATION,
              StatutSinistre.CLOS,
              StatutSinistre.CLASSE_SANS_SUITE),
          StatutSinistre.DECLARE_ASSUREUR,
          Set.of(
              StatutSinistre.EN_EXPERTISE,
              StatutSinistre.EN_REPARATION,
              StatutSinistre.CLOS,
              StatutSinistre.CLASSE_SANS_SUITE),
          StatutSinistre.EN_EXPERTISE,
          Set.of(StatutSinistre.EN_REPARATION, StatutSinistre.CLOS),
          StatutSinistre.EN_REPARATION,
          Set.of(StatutSinistre.CLOS),
          StatutSinistre.CLOS,
          Set.of(),
          StatutSinistre.CLASSE_SANS_SUITE,
          Set.of());

  /** Circonstances et parties, modifiables tant que le sinistre n'est pas clos. */
  public record Circonstances(
      UUID vehiculeId,
      UUID remorqueId,
      UUID chauffeurId,
      UUID voyageId,
      LocalDateTime dateSurvenance,
      String lieu,
      GeoPoint position,
      TypeSinistre type,
      GraviteSinistre gravite,
      Responsabilite responsabilite,
      String description,
      boolean constatAmiable,
      boolean rapportPolice,
      boolean blesses,
      boolean enginImmobilise,
      Tiers tiers) {

    public Circonstances {
      if (vehiculeId == null && remorqueId == null) {
        throw new IllegalArgumentException(
            "Un sinistre concerne au moins un véhicule ou une remorque");
      }
      Objects.requireNonNull(dateSurvenance, "La date du sinistre est obligatoire");
      Objects.requireNonNull(type, "Le type de sinistre est obligatoire");
      Objects.requireNonNull(gravite, "La gravité est obligatoire");
      responsabilite = responsabilite == null ? Responsabilite.A_DETERMINER : responsabilite;
      if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("La description des circonstances est obligatoire");
      }
      description = description.strip();
    }
  }

  /** Suivi assurance et montants. */
  public record SuiviAssurance(
      UUID contratId,
      String numeroDossierAssureur,
      LocalDate dateDeclarationAssureur,
      UUID expertId,
      LocalDate dateExpertise,
      Money estimationDommages,
      Money franchise,
      Money indemnite) {

    public static SuiviAssurance vide() {
      return new SuiviAssurance(null, null, null, null, null, null, null, null);
    }
  }

  private final UUID id;
  private final Reference reference;
  private Circonstances circonstances;
  private SuiviAssurance assurance;
  private StatutSinistre statut;
  private LocalDate dateCloture;

  private Sinistre(
      UUID id,
      Reference reference,
      Circonstances circonstances,
      SuiviAssurance assurance,
      StatutSinistre statut,
      LocalDate dateCloture) {
    this.id = Objects.requireNonNull(id, "L'identifiant du sinistre est obligatoire");
    this.reference = Objects.requireNonNull(reference, "La référence est obligatoire");
    this.circonstances =
        Objects.requireNonNull(circonstances, "Les circonstances sont obligatoires");
    this.assurance = Objects.requireNonNull(assurance, "Le suivi assurance est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.dateCloture = dateCloture;
  }

  public static Sinistre declarer(
      UUID id, Reference reference, Circonstances circonstances, SuiviAssurance assurance) {
    return new Sinistre(id, reference, circonstances, assurance, StatutSinistre.DECLARE, null);
  }

  public static Sinistre reconstituer(
      UUID id,
      Reference reference,
      Circonstances circonstances,
      SuiviAssurance assurance,
      StatutSinistre statut,
      LocalDate dateCloture) {
    return new Sinistre(id, reference, circonstances, assurance, statut, dateCloture);
  }

  public void modifier(Circonstances nouvelles, SuiviAssurance suivi) {
    if (estTermine()) {
      throw new BusinessException("Un sinistre clos ou classé n'est plus modifiable");
    }
    this.circonstances = Objects.requireNonNull(nouvelles, "Les circonstances sont obligatoires");
    this.assurance = Objects.requireNonNull(suivi, "Le suivi assurance est obligatoire");
  }

  /**
   * Change de statut. Déclarer à l'assureur exige la date de déclaration ; clore exige que les
   * réparations liées soient terminées ou annulées ({@code reparationsOuvertes} = 0).
   */
  public void changerStatut(
      StatutSinistre nouveau, LocalDate aujourdHui, long reparationsOuvertes) {
    Objects.requireNonNull(nouveau, "Le nouveau statut est obligatoire");
    if (!TRANSITIONS.getOrDefault(statut, Set.of()).contains(nouveau)) {
      throw new BusinessException(
          "Transition de statut invalide pour le sinistre : %s -> %s".formatted(statut, nouveau));
    }
    if (nouveau == StatutSinistre.DECLARE_ASSUREUR && assurance.dateDeclarationAssureur() == null) {
      throw new BusinessException("Renseignez la date de déclaration à l'assureur");
    }
    if (nouveau == StatutSinistre.CLOS && reparationsOuvertes > 0) {
      throw new BusinessException(
          "Le sinistre a encore " + reparationsOuvertes + " ordre(s) de travail non terminé(s)");
    }
    this.statut = nouveau;
    if (estTermine()) {
      this.dateCloture = aujourdHui;
    }
  }

  public boolean estTermine() {
    return statut == StatutSinistre.CLOS || statut == StatutSinistre.CLASSE_SANS_SUITE;
  }

  /** Déclaration à l'assureur en retard : non déclarée au-delà du délai en jours ouvrés. */
  public boolean declarationEnRetard(LocalDate aujourdHui) {
    if (assurance.dateDeclarationAssureur() != null || estTermine()) {
      return false;
    }
    return joursOuvres(circonstances.dateSurvenance().toLocalDate(), aujourdHui)
        > DELAI_DECLARATION_JOURS_OUVRES;
  }

  static long joursOuvres(LocalDate debut, LocalDate fin) {
    long jours = 0;
    for (LocalDate d = debut.plusDays(1); !d.isAfter(fin); d = d.plusDays(1)) {
      if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
        jours++;
      }
    }
    return jours;
  }

  /** Coût net pour l'entreprise : réparations réalisées moins l'indemnité perçue. */
  public Money coutNet(Money totalReparations) {
    Money indemnite = assurance.indemnite();
    return indemnite == null ? totalReparations : totalReparations.moins(indemnite);
  }

  public UUID id() {
    return id;
  }

  public Reference reference() {
    return reference;
  }

  public Circonstances circonstances() {
    return circonstances;
  }

  public SuiviAssurance assurance() {
    return assurance;
  }

  public StatutSinistre statut() {
    return statut;
  }

  public LocalDate dateCloture() {
    return dateCloture;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Sinistre autre && id.equals(autre.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

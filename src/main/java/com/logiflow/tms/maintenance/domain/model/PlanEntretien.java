package com.logiflow.tms.maintenance.domain.model;

import com.logiflow.tms.maintenance.domain.vo.Echeance;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Plan d'entretien périodique d'un engin (kilomètres, mois et/ou heures de fonctionnement). La
 * dernière réalisation est mise à jour à la clôture d'un ordre de travail lié ; la prochaine
 * échéance en découle.
 */
public final class PlanEntretien {

  /** Paramètres du plan, modifiables. */
  public record Parametres(
      String libelle,
      TypeIntervention type,
      Integer periodiciteKm,
      Integer periodiciteMois,
      Integer periodiciteHeures,
      int seuilAlerteKm,
      int seuilAlerteJours,
      int dureeEstimeeMin,
      Money coutEstime,
      UUID prestataireId,
      boolean actif) {

    public Parametres {
      if (libelle == null || libelle.isBlank()) {
        throw new IllegalArgumentException("Le libellé du plan est obligatoire");
      }
      libelle = libelle.strip();
      type = type == null ? TypeIntervention.ENTRETIEN_PREVENTIF : type;
      if (periodiciteKm == null && periodiciteMois == null && periodiciteHeures == null) {
        throw new IllegalArgumentException(
            "Un plan d'entretien doit définir une périodicité en kilomètres, mois ou heures");
      }
      if (Stream.of(periodiciteKm, periodiciteMois, periodiciteHeures)
          .anyMatch(p -> p != null && p <= 0)) {
        throw new IllegalArgumentException("Les périodicités doivent être strictement positives");
      }
      if (seuilAlerteKm < 0 || seuilAlerteJours < 0 || dureeEstimeeMin < 0) {
        throw new IllegalArgumentException("Seuils et durée ne peuvent pas être négatifs");
      }
    }
  }

  /** Dernière réalisation connue (null = jamais réalisé depuis la création du plan). */
  public record DerniereRealisation(LocalDate date, Integer kilometrage, Integer heures) {}

  private final UUID id;
  private final EnginRef engin;
  private Parametres parametres;
  private DerniereRealisation derniereRealisation;

  private PlanEntretien(
      UUID id, EnginRef engin, Parametres parametres, DerniereRealisation derniereRealisation) {
    this.id = Objects.requireNonNull(id, "L'identifiant du plan d'entretien est obligatoire");
    this.engin = Objects.requireNonNull(engin, "L'engin est obligatoire");
    this.parametres = Objects.requireNonNull(parametres, "Les paramètres sont obligatoires");
    this.derniereRealisation = derniereRealisation;
  }

  public static PlanEntretien creer(
      UUID id, EnginRef engin, Parametres parametres, DerniereRealisation derniereRealisation) {
    return new PlanEntretien(id, engin, parametres, derniereRealisation);
  }

  public static PlanEntretien reconstituer(
      UUID id, EnginRef engin, Parametres parametres, DerniereRealisation derniereRealisation) {
    return new PlanEntretien(id, engin, parametres, derniereRealisation);
  }

  public void modifier(Parametres nouveaux) {
    this.parametres = Objects.requireNonNull(nouveaux, "Les paramètres sont obligatoires");
  }

  /** Enregistre une réalisation (clôture d'un OT lié) si elle est plus récente que la dernière. */
  public void enregistrerRealisation(LocalDate date, Integer kilometrage, Integer heures) {
    Objects.requireNonNull(date, "La date de réalisation est obligatoire");
    if (derniereRealisation == null || !date.isBefore(derniereRealisation.date())) {
      this.derniereRealisation = new DerniereRealisation(date, kilometrage, heures);
    }
  }

  /**
   * Prochaine échéance selon les compteurs actuels et l'usage moyen ({@code kmParJour} pour
   * projeter une date à partir des kilomètres restants).
   *
   * <p>Sans réalisation connue, l'origine du cycle est estimée par la position du compteur dans la
   * périodicité (compteur modulo périodicité) ; aucune échéance calendaire n'est alors calculée.
   */
  public Echeance prochaineEcheance(
      int kmActuel, int heuresActuelles, LocalDate aujourdHui, double kmParJour) {
    Integer kmRestant = null;
    if (parametres.periodiciteKm() != null) {
      int kmDepuis =
          derniereRealisation != null && derniereRealisation.kilometrage() != null
              ? kmActuel - derniereRealisation.kilometrage()
              : kmActuel % parametres.periodiciteKm();
      kmRestant = parametres.periodiciteKm() - Math.max(kmDepuis, 0);
    }
    Integer heuresRestantes = null;
    if (parametres.periodiciteHeures() != null) {
      int heuresDepuis =
          derniereRealisation != null && derniereRealisation.heures() != null
              ? heuresActuelles - derniereRealisation.heures()
              : heuresActuelles % parametres.periodiciteHeures();
      heuresRestantes = parametres.periodiciteHeures() - Math.max(heuresDepuis, 0);
    }
    LocalDate date = null;
    if (parametres.periodiciteMois() != null && derniereRealisation != null) {
      date = derniereRealisation.date().plusMonths(parametres.periodiciteMois());
    }
    if (kmRestant != null && kmParJour > 0) {
      LocalDate parKm = aujourdHui.plusDays((long) Math.ceil(Math.max(kmRestant, 0) / kmParJour));
      date = date == null || parKm.isBefore(date) ? parKm : date;
    }
    return new Echeance(
        kmRestant, heuresRestantes, date, etat(kmRestant, heuresRestantes, date, aujourdHui));
  }

  private EtatEcheance etat(
      Integer kmRestant, Integer heuresRestantes, LocalDate date, LocalDate aujourdHui) {
    boolean echu =
        (kmRestant != null && kmRestant <= 0)
            || (heuresRestantes != null && heuresRestantes <= 0)
            || (date != null && !date.isAfter(aujourdHui));
    if (echu) {
      return EtatEcheance.ECHU;
    }
    boolean alerte =
        (kmRestant != null && kmRestant <= parametres.seuilAlerteKm())
            || (date != null && !date.isAfter(aujourdHui.plusDays(parametres.seuilAlerteJours())));
    return alerte ? EtatEcheance.ALERTE : EtatEcheance.OK;
  }

  public UUID id() {
    return id;
  }

  public EnginRef engin() {
    return engin;
  }

  public Parametres parametres() {
    return parametres;
  }

  public DerniereRealisation derniereRealisation() {
    return derniereRealisation;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof PlanEntretien autre && id.equals(autre.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

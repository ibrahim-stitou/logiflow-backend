package com.logiflow.tms.referential.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/** Horaires d'ouverture d'un site, exprimés en créneaux hebdomadaires. */
public final class Horaires {

  private final List<CreneauHoraire> creneaux;

  private Horaires(List<CreneauHoraire> creneaux) {
    this.creneaux = List.copyOf(creneaux);
  }

  public static Horaires aucun() {
    return new Horaires(List.of());
  }

  public static Horaires de(List<CreneauHoraire> creneaux) {
    Objects.requireNonNull(creneaux, "La liste de créneaux est obligatoire");
    return new Horaires(creneaux);
  }

  public List<CreneauHoraire> creneaux() {
    return creneaux;
  }

  /** Indique si le site est ouvert au jour et à l'heure donnés. */
  public boolean estOuvertA(DayOfWeek jour, LocalTime heure) {
    Objects.requireNonNull(jour, "Le jour est obligatoire");
    Objects.requireNonNull(heure, "L'heure est obligatoire");
    return creneaux.stream()
        .anyMatch(
            creneau ->
                creneau.jour() == jour
                    && !heure.isBefore(creneau.debut())
                    && heure.isBefore(creneau.fin()));
  }

  /** Un créneau d'ouverture pour un jour de la semaine donné. */
  public record CreneauHoraire(DayOfWeek jour, LocalTime debut, LocalTime fin) {
    public CreneauHoraire {
      Objects.requireNonNull(jour, "Le jour du créneau est obligatoire");
      Objects.requireNonNull(debut, "L'heure de début du créneau est obligatoire");
      Objects.requireNonNull(fin, "L'heure de fin du créneau est obligatoire");
      if (!fin.isAfter(debut)) {
        throw new IllegalArgumentException(
            "L'heure de fin du créneau doit être postérieure à l'heure de début");
      }
    }
  }
}

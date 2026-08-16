package com.logiflow.tms.planning.domain.model;

import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Voyage : exécution physique d'une ressource (véhicule + remorque + chauffeur) pour 1 à n dossiers
 * de transport. Entité racine du module {@code planning}.
 */
public final class Voyage {

  private static final Map<StatutVoyage, Set<StatutVoyage>> TRANSITIONS_AUTORISEES =
      Map.of(
          StatutVoyage.BROUILLON, Set.of(StatutVoyage.PLANIFIE, StatutVoyage.ANNULE),
          StatutVoyage.PLANIFIE, Set.of(StatutVoyage.AFFECTE, StatutVoyage.ANNULE),
          StatutVoyage.AFFECTE, Set.of(StatutVoyage.EN_COURS, StatutVoyage.ANNULE),
          StatutVoyage.EN_COURS, Set.of(StatutVoyage.TERMINE),
          StatutVoyage.TERMINE, Set.of(StatutVoyage.CLOTURE),
          StatutVoyage.CLOTURE, Set.of(),
          StatutVoyage.ANNULE, Set.of());

  private final UUID id;
  private final Reference reference;
  private final TypeVoyage typeVoyage;
  private final Portee portee;
  private StatutVoyage statut;
  private final Instant departPrevu;
  private final Instant arriveePrevue;
  private final UUID vehiculeId;
  private final UUID remorqueId;
  private final List<UUID> dossierIds;
  private Trajet trajet;
  private List<Affectation> affectations;
  private double tauxRemplissage;

  private Voyage(
      UUID id,
      Reference reference,
      TypeVoyage typeVoyage,
      Portee portee,
      StatutVoyage statut,
      Instant departPrevu,
      Instant arriveePrevue,
      UUID vehiculeId,
      UUID remorqueId,
      List<UUID> dossierIds,
      Trajet trajet,
      List<Affectation> affectations,
      double tauxRemplissage) {
    this.id = Objects.requireNonNull(id, "L'identifiant du voyage est obligatoire");
    this.reference = Objects.requireNonNull(reference, "La référence du voyage est obligatoire");
    this.typeVoyage = Objects.requireNonNull(typeVoyage, "Le type de voyage est obligatoire");
    this.portee = Objects.requireNonNull(portee, "La portée est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.departPrevu =
        Objects.requireNonNull(departPrevu, "La date de départ prévue est obligatoire");
    this.arriveePrevue =
        Objects.requireNonNull(arriveePrevue, "La date d'arrivée prévue est obligatoire");
    if (!arriveePrevue.isAfter(departPrevu)) {
      throw new IllegalArgumentException("L'arrivée prévue doit être postérieure au départ prévu");
    }
    this.vehiculeId = Objects.requireNonNull(vehiculeId, "Le véhicule est obligatoire");
    this.remorqueId = remorqueId;
    this.dossierIds = List.copyOf(dossierIds);
    if (this.dossierIds.isEmpty()) {
      throw new IllegalArgumentException("Un voyage doit transporter au moins un dossier");
    }
    this.trajet = Objects.requireNonNull(trajet, "Le trajet est obligatoire");
    this.affectations = List.copyOf(affectations);
    if (this.affectations.isEmpty()) {
      throw new IllegalArgumentException(
          "Un voyage doit avoir au moins une affectation de chauffeur");
    }
    if (tauxRemplissage < 0 || tauxRemplissage > 1) {
      throw new IllegalArgumentException("Le taux de remplissage doit être compris entre 0 et 1");
    }
    this.tauxRemplissage = tauxRemplissage;
  }

  public static Voyage creer(
      UUID id,
      Reference reference,
      TypeVoyage typeVoyage,
      Portee portee,
      Instant departPrevu,
      Instant arriveePrevue,
      UUID vehiculeId,
      UUID remorqueId,
      List<UUID> dossierIds,
      Trajet trajet,
      List<Affectation> affectations,
      double tauxRemplissage) {
    return new Voyage(
        id,
        reference,
        typeVoyage,
        portee,
        StatutVoyage.BROUILLON,
        departPrevu,
        arriveePrevue,
        vehiculeId,
        remorqueId,
        dossierIds,
        trajet,
        affectations,
        tauxRemplissage);
  }

  public static Voyage reconstituer(
      UUID id,
      Reference reference,
      TypeVoyage typeVoyage,
      Portee portee,
      StatutVoyage statut,
      Instant departPrevu,
      Instant arriveePrevue,
      UUID vehiculeId,
      UUID remorqueId,
      List<UUID> dossierIds,
      Trajet trajet,
      List<Affectation> affectations,
      double tauxRemplissage) {
    return new Voyage(
        id,
        reference,
        typeVoyage,
        portee,
        statut,
        departPrevu,
        arriveePrevue,
        vehiculeId,
        remorqueId,
        dossierIds,
        trajet,
        affectations,
        tauxRemplissage);
  }

  /** Applique une transition d'état, en la validant contre le cycle de vie autorisé. */
  public void changerStatut(StatutVoyage nouveauStatut) {
    Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
    if (!TRANSITIONS_AUTORISEES.getOrDefault(statut, Set.of()).contains(nouveauStatut)) {
      throw new BusinessException(
          "Transition de statut invalide pour le voyage %s : %s -> %s"
              .formatted(reference.valeur(), statut, nouveauStatut));
    }
    this.statut = nouveauStatut;
  }

  public void mettreAJourTrajet(Trajet trajet) {
    this.trajet = Objects.requireNonNull(trajet, "Le trajet est obligatoire");
  }

  public void mettreAJourRemplissage(double tauxRemplissage) {
    if (tauxRemplissage < 0 || tauxRemplissage > 1) {
      throw new IllegalArgumentException("Le taux de remplissage doit être compris entre 0 et 1");
    }
    this.tauxRemplissage = tauxRemplissage;
  }

  public boolean estGroupage() {
    return dossierIds.size() > 1;
  }

  public UUID id() {
    return id;
  }

  public Reference reference() {
    return reference;
  }

  public TypeVoyage typeVoyage() {
    return typeVoyage;
  }

  public Portee portee() {
    return portee;
  }

  public StatutVoyage statut() {
    return statut;
  }

  public Instant departPrevu() {
    return departPrevu;
  }

  public Instant arriveePrevue() {
    return arriveePrevue;
  }

  public UUID vehiculeId() {
    return vehiculeId;
  }

  public UUID remorqueId() {
    return remorqueId;
  }

  public List<UUID> dossierIds() {
    return dossierIds;
  }

  public Trajet trajet() {
    return trajet;
  }

  public List<Affectation> affectations() {
    return affectations;
  }

  public double tauxRemplissage() {
    return tauxRemplissage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Voyage voyage)) {
      return false;
    }
    return id.equals(voyage.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

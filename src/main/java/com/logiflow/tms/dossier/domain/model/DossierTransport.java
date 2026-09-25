package com.logiflow.tms.dossier.domain.model;

import com.logiflow.tms.dossier.domain.vo.DocumentTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Dossier de transport : unité transportable et facturable issue d'une commande. Entité racine du
 * module {@code dossier}.
 */
public final class DossierTransport {

  private static final Map<StatutDossier, Set<StatutDossier>> TRANSITIONS_AUTORISEES =
      Map.of(
          StatutDossier.CREE, Set.of(StatutDossier.PLANIFIE, StatutDossier.ANNULE),
          StatutDossier.PLANIFIE,
              Set.of(StatutDossier.CREE, StatutDossier.EN_CHARGEMENT, StatutDossier.ANNULE),
          StatutDossier.EN_CHARGEMENT, Set.of(StatutDossier.CHARGE, StatutDossier.ANNULE),
          StatutDossier.CHARGE, Set.of(StatutDossier.EN_TRANSIT, StatutDossier.ANNULE),
          StatutDossier.EN_TRANSIT, Set.of(StatutDossier.EN_LIVRAISON, StatutDossier.INCIDENT),
          StatutDossier.EN_LIVRAISON, Set.of(StatutDossier.LIVRE),
          StatutDossier.LIVRE, Set.of(StatutDossier.CLOTURE),
          StatutDossier.INCIDENT, Set.of(StatutDossier.EN_TRANSIT, StatutDossier.ANNULE),
          StatutDossier.CLOTURE, Set.of(),
          StatutDossier.ANNULE, Set.of());

  private final UUID id;
  private final Reference reference;
  private final UUID commandeId;
  private StatutDossier statut;
  private final TypeTransport typeTransport;
  private final boolean groupable;
  private final double poidsBrutKg;
  private final double volumeM3;
  private final int nbPalettes;
  private final String familleMarchandise;
  private final TypeCarrosserieRequise carrosserieRequise;
  private final Double temperatureRequise;
  private List<LigneMarchandise> lignesMarchandise;
  private List<Segment> segments;
  private List<DocumentTransport> documents;
  private UUID arretChargementId;
  private UUID arretDechargementId;

  private DossierTransport(
      UUID id,
      Reference reference,
      UUID commandeId,
      StatutDossier statut,
      TypeTransport typeTransport,
      boolean groupable,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      String familleMarchandise,
      TypeCarrosserieRequise carrosserieRequise,
      Double temperatureRequise,
      List<LigneMarchandise> lignesMarchandise,
      List<Segment> segments,
      List<DocumentTransport> documents,
      UUID arretChargementId,
      UUID arretDechargementId) {
    this.id = Objects.requireNonNull(id, "L'identifiant du dossier est obligatoire");
    this.reference = Objects.requireNonNull(reference, "La référence du dossier est obligatoire");
    this.commandeId = Objects.requireNonNull(commandeId, "La commande d'origine est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.typeTransport =
        Objects.requireNonNull(typeTransport, "Le type de transport est obligatoire");
    this.groupable = groupable;
    if (poidsBrutKg < 0) {
      throw new IllegalArgumentException("Le poids brut ne peut pas être négatif");
    }
    if (volumeM3 < 0) {
      throw new IllegalArgumentException("Le volume ne peut pas être négatif");
    }
    if (nbPalettes < 0) {
      throw new IllegalArgumentException("Le nombre de palettes ne peut pas être négatif");
    }
    this.poidsBrutKg = poidsBrutKg;
    this.volumeM3 = volumeM3;
    this.nbPalettes = nbPalettes;
    this.familleMarchandise =
        Objects.requireNonNull(familleMarchandise, "La famille de marchandise est obligatoire");
    this.carrosserieRequise = carrosserieRequise;
    this.temperatureRequise = temperatureRequise;
    this.lignesMarchandise = List.copyOf(lignesMarchandise);
    if (this.lignesMarchandise.isEmpty()) {
      throw new IllegalArgumentException(
          "Un dossier de transport doit contenir au moins une ligne de marchandise");
    }
    this.segments = List.copyOf(segments);
    if (this.segments.size() < 2) {
      throw new IllegalArgumentException(
          "Un dossier de transport doit définir au moins deux segments (chargement et déchargement)");
    }
    this.documents = List.copyOf(documents);
    this.arretChargementId = arretChargementId;
    this.arretDechargementId = arretDechargementId;
  }

  public static DossierTransport creer(
      UUID id,
      Reference reference,
      UUID commandeId,
      TypeTransport typeTransport,
      boolean groupable,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      String familleMarchandise,
      TypeCarrosserieRequise carrosserieRequise,
      Double temperatureRequise,
      List<LigneMarchandise> lignesMarchandise,
      List<Segment> segments,
      List<DocumentTransport> documents) {
    return new DossierTransport(
        id,
        reference,
        commandeId,
        StatutDossier.CREE,
        typeTransport,
        groupable,
        poidsBrutKg,
        volumeM3,
        nbPalettes,
        familleMarchandise,
        carrosserieRequise,
        temperatureRequise,
        lignesMarchandise,
        segments,
        documents,
        null,
        null);
  }

  public static DossierTransport reconstituer(
      UUID id,
      Reference reference,
      UUID commandeId,
      StatutDossier statut,
      TypeTransport typeTransport,
      boolean groupable,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      String familleMarchandise,
      TypeCarrosserieRequise carrosserieRequise,
      Double temperatureRequise,
      List<LigneMarchandise> lignesMarchandise,
      List<Segment> segments,
      List<DocumentTransport> documents,
      UUID arretChargementId,
      UUID arretDechargementId) {
    return new DossierTransport(
        id,
        reference,
        commandeId,
        statut,
        typeTransport,
        groupable,
        poidsBrutKg,
        volumeM3,
        nbPalettes,
        familleMarchandise,
        carrosserieRequise,
        temperatureRequise,
        lignesMarchandise,
        segments,
        documents,
        arretChargementId,
        arretDechargementId);
  }

  /**
   * Associe les arrêts voyage de chargement et de déchargement pour le calcul de capacité par
   * tronçon. Appelé lors de la planification ou de l'ajout au voyage.
   */
  public void affecterArretsVoyage(UUID chargementId, UUID dechargementId) {
    Objects.requireNonNull(chargementId, "L'arrêt de chargement est obligatoire");
    Objects.requireNonNull(dechargementId, "L'arrêt de déchargement est obligatoire");
    this.arretChargementId = chargementId;
    this.arretDechargementId = dechargementId;
  }

  /** Réinitialise les arrêts voyage (ex. annulation de planification). */
  public void retirerArretsVoyage() {
    this.arretChargementId = null;
    this.arretDechargementId = null;
  }

  /** Applique une transition d'état, en la validant contre le cycle de vie autorisé. */
  public void changerStatut(StatutDossier nouveauStatut) {
    Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
    if (!TRANSITIONS_AUTORISEES.getOrDefault(statut, Set.of()).contains(nouveauStatut)) {
      throw new BusinessException(
          "Transition de statut invalide pour le dossier %s : %s -> %s"
              .formatted(reference.valeur(), statut, nouveauStatut));
    }
    this.statut = nouveauStatut;
  }

  public void mettreAJourDocuments(List<DocumentTransport> documents) {
    this.documents = List.copyOf(documents);
  }

  /** Capacité de chargement requise pour transporter ce dossier. */
  public Capacite capaciteRequise() {
    return new Capacite((int) Math.round(poidsBrutKg), volumeM3, nbPalettes);
  }

  /**
   * Indique si le dossier contient une matière dangereuse, en résolvant pour chaque ligne sans
   * surcharge le classement par défaut de sa marchandise via le prédicat fourni (module {@code
   * referential}).
   */
  public boolean contientAdr(java.util.function.Predicate<UUID> marchandiseEstDangereuse) {
    return lignesMarchandise.stream()
        .anyMatch(
            ligne ->
                ligne.estMatiereDangereuse(marchandiseEstDangereuse.test(ligne.marchandiseId())));
  }

  /**
   * Compatibilité de groupage "règles dures" avec un autre dossier : même type de transport,
   * carrosserie et plage de température compatibles. Le score fin (proximité géographique, fenêtres
   * horaires, gain économique) est calculé par le module {@code planning}.
   */
  public boolean estGroupableAvec(DossierTransport autre) {
    Objects.requireNonNull(autre, "Le dossier à comparer est obligatoire");
    if (!this.groupable || !autre.groupable) {
      return false;
    }
    if (this.typeTransport != autre.typeTransport) {
      return false;
    }
    if (this.carrosserieRequise != null
        && autre.carrosserieRequise != null
        && this.carrosserieRequise != autre.carrosserieRequise) {
      return false;
    }
    return this.temperatureRequise == null
        || autre.temperatureRequise == null
        || this.temperatureRequise.equals(autre.temperatureRequise);
  }

  public UUID id() {
    return id;
  }

  public Reference reference() {
    return reference;
  }

  public UUID commandeId() {
    return commandeId;
  }

  public StatutDossier statut() {
    return statut;
  }

  public TypeTransport typeTransport() {
    return typeTransport;
  }

  public boolean groupable() {
    return groupable;
  }

  public double poidsBrutKg() {
    return poidsBrutKg;
  }

  public double volumeM3() {
    return volumeM3;
  }

  public int nbPalettes() {
    return nbPalettes;
  }

  public String familleMarchandise() {
    return familleMarchandise;
  }

  public TypeCarrosserieRequise carrosserieRequise() {
    return carrosserieRequise;
  }

  public Double temperatureRequise() {
    return temperatureRequise;
  }

  public List<LigneMarchandise> lignesMarchandise() {
    return lignesMarchandise;
  }

  public List<Segment> segments() {
    return segments;
  }

  public List<DocumentTransport> documents() {
    return documents;
  }

  public UUID arretChargementId() {
    return arretChargementId;
  }

  public UUID arretDechargementId() {
    return arretDechargementId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DossierTransport dossier)) {
      return false;
    }
    return id.equals(dossier.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

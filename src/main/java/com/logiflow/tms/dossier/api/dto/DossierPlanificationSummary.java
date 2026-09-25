package com.logiflow.tms.dossier.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Vue d'un dossier pour la planification de voyage (création manuelle et agent) : charge, exigences
 * de transport et segments ordonnés. {@code carrosserieRequise} et {@code temperatureRequise}
 * valent null quand le dossier n'impose rien.
 */
public record DossierPlanificationSummary(
    UUID id,
    String reference,
    String statut,
    String typeTransport,
    boolean groupable,
    double poidsBrutKg,
    double volumeM3,
    int nbPalettes,
    boolean contientAdr,
    String carrosserieRequise,
    Double temperatureRequise,
    List<SegmentPlanificationSummary> segments) {

  public DossierPlanificationSummary {
    segments = List.copyOf(segments);
  }

  /** Premier segment de chargement (ordre croissant), null si absent. */
  public SegmentPlanificationSummary chargement() {
    return segments.stream()
        .filter(s -> "CHARGEMENT".equals(s.type()))
        .min(java.util.Comparator.comparingInt(SegmentPlanificationSummary::ordre))
        .orElse(null);
  }

  /** Dernier segment de déchargement (ordre croissant), null si absent. */
  public SegmentPlanificationSummary dechargement() {
    return segments.stream()
        .filter(s -> "DECHARGEMENT".equals(s.type()))
        .max(java.util.Comparator.comparingInt(SegmentPlanificationSummary::ordre))
        .orElse(null);
  }

  public boolean international() {
    return !"NATIONAL".equals(typeTransport);
  }
}

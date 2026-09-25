package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des ordres de travail. */
public interface OrdreTravailRepository {

  /** Critères de recherche, tous facultatifs (null = pas de filtre). */
  record Filtre(
      TypeEngin typeEngin,
      UUID enginId,
      StatutOT statut,
      TypeIntervention type,
      NatureIntervention nature,
      UUID prestataireId,
      String texte,
      LocalDateTime debut,
      LocalDateTime fin) {

    public static Filtre aucun() {
      return new Filtre(null, null, null, null, null, null, null, null, null);
    }
  }

  OrdreTravail sauvegarder(OrdreTravail ordreTravail);

  Optional<OrdreTravail> parId(UUID id);

  /** Recherche paginée, du début planifié le plus récent au plus ancien. */
  Page<OrdreTravail> rechercher(Filtre filtre, PageRequest pageRequest);

  /** Tous les OT répondant au filtre (calcul de coûts, analyse) — sans pagination. */
  List<OrdreTravail> lister(Filtre filtre);

  List<OrdreTravail> parPlanId(UUID planId);

  List<OrdreTravail> parSinistreId(UUID sinistreId);
}

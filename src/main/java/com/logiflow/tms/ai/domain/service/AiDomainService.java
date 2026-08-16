package com.logiflow.tms.ai.domain.service;

import com.logiflow.tms.ai.domain.model.CandidatDossier;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import java.util.List;
import java.util.Objects;

/**
 * Règles de repli du module {@code ai}, appliquées lorsque le service IA externe est indisponible.
 * Volontairement plus simples que les règles dures du module {@code dossier} ({@code
 * DossierTransport.estGroupableAvec}) : ce module ne dépend jamais du domaine d'un autre module
 * (règle d'architecture 5) et ne travaille qu'avec les champs exposés par {@code
 * dossier.api.DossierApi}.
 */
public class AiDomainService {

  /**
   * En l'absence d'analyse IA, propose un unique regroupement de tous les dossiers marqués
   * groupables, sans score ni justification métier fine.
   */
  public List<PropositionGroupage> repliSansIa(List<CandidatDossier> candidats) {
    Objects.requireNonNull(candidats, "La liste de candidats est obligatoire");
    List<java.util.UUID> groupables =
        candidats.stream().filter(CandidatDossier::groupable).map(CandidatDossier::id).toList();
    if (groupables.size() < 2) {
      return List.of();
    }
    return List.of(
        new PropositionGroupage(
            groupables,
            null,
            null,
            null,
            null,
            "Analyse IA indisponible : regroupement proposé sur la seule compatibilité de base (dossiers marqués groupables). "
                + "À affiner manuellement avant validation.",
            false));
  }
}

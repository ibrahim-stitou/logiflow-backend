package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.CandidatDossier;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import com.logiflow.tms.ai.domain.model.ReponseCopilote;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.util.List;
import java.util.Set;

/**
 * Port de sortie vers le service IA externe (application Flask). Implémenté en infrastructure par
 * un adaptateur HTTP ; le domaine ignore tout du protocole de transport utilisé.
 */
public interface AiServiceClientPort {

  /**
   * @throws ServiceIndisponibleException si le service IA ne peut pas être joint ou répond en
   *     erreur
   */
  ReponseCopilote poserQuestion(String question, String utilisateurId, Set<String> roles);

  /**
   * @throws ServiceIndisponibleException si le service IA ne peut pas être joint ou répond en
   *     erreur
   */
  List<PropositionGroupage> analyserGroupage(List<CandidatDossier> candidats);
}

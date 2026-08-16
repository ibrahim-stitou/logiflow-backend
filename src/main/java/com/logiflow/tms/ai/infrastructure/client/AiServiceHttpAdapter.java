package com.logiflow.tms.ai.infrastructure.client;

import com.logiflow.tms.ai.domain.model.CandidatDossier;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import com.logiflow.tms.ai.domain.model.ReponseCopilote;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.infrastructure.client.dto.CopilotAskRequest;
import com.logiflow.tms.ai.infrastructure.client.dto.CopilotAskResponse;
import com.logiflow.tms.ai.infrastructure.client.dto.GroupageAnalyserRequest;
import com.logiflow.tms.ai.infrastructure.client.dto.GroupageAnalyserRequest.DossierCandidatDto;
import com.logiflow.tms.ai.infrastructure.client.dto.GroupageAnalyserResponse;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.infrastructure.web.CorrelationIdFilter;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur HTTP vers le service IA externe (application Flask). Toute défaillance réseau ou
 * réponse en erreur est traduite en {@link ServiceIndisponibleException}, seule exception connue de
 * la couche application — celle-ci décide (selon le cas d'usage) de la propager ou de se replier
 * sur un comportement déterministe. Voir docs/integration-ia.md pour le contrat détaillé.
 */
@Component
@RequiredArgsConstructor
public class AiServiceHttpAdapter implements AiServiceClientPort {

  private final RestClient aiServiceRestClient;

  @Override
  public ReponseCopilote poserQuestion(String question, String utilisateurId, Set<String> roles) {
    try {
      CopilotAskResponse reponse =
          aiServiceRestClient
              .post()
              .uri("/internal/ai/v1/copilot/ask")
              .body(
                  new CopilotAskRequest(
                      question,
                      new CopilotAskRequest.UtilisateurDto(utilisateurId, roles),
                      CorrelationIdFilter.correlationIdCourant()))
              .retrieve()
              .body(CopilotAskResponse.class);
      if (reponse == null) {
        throw new ServiceIndisponibleException("Réponse vide du service IA (copilote)");
      }
      return new ReponseCopilote(reponse.reponse(), reponse.sources(), reponse.confiance());
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le service IA (copilote) est momentanément indisponible", e);
    }
  }

  @Override
  public List<PropositionGroupage> analyserGroupage(List<CandidatDossier> candidats) {
    try {
      List<DossierCandidatDto> dossiers =
          candidats.stream()
              .map(
                  c ->
                      new DossierCandidatDto(
                          c.id(),
                          c.reference(),
                          c.poidsBrutKg(),
                          c.volumeM3(),
                          c.nbPalettes(),
                          c.contientAdr()))
              .toList();
      GroupageAnalyserResponse reponse =
          aiServiceRestClient
              .post()
              .uri("/internal/ai/v1/groupage/analyser")
              .body(
                  new GroupageAnalyserRequest(dossiers, CorrelationIdFilter.correlationIdCourant()))
              .retrieve()
              .body(GroupageAnalyserResponse.class);
      if (reponse == null) {
        throw new ServiceIndisponibleException("Réponse vide du service IA (groupage)");
      }
      return reponse.propositions().stream()
          .map(
              p ->
                  new PropositionGroupage(
                      p.dossierIds(),
                      p.score(),
                      p.confiance(),
                      p.gainKm(),
                      p.gainMarge(),
                      p.justification(),
                      true))
          .toList();
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le service IA (groupage) est momentanément indisponible", e);
    }
  }
}

package com.logiflow.tms.ai.infrastructure.client;

import com.logiflow.tms.ai.domain.model.planification.ContextePlanification;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification;
import com.logiflow.tms.ai.domain.port.out.PlanificationClientPort;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.infrastructure.web.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur HTTP vers l'agent de planification ({@code POST
 * /internal/ai/v1/planification/proposer}). Le contrat JSON est porté directement par les records
 * du domaine. Utilise le client au délai de lecture long : l'agent enchaîne matrice OSRM, solveur
 * et rédaction par LLM.
 */
@Component
public class PlanificationHttpAdapter implements PlanificationClientPort {

  private final RestClient client;

  public PlanificationHttpAdapter(
      @Qualifier("aiServiceStreamRestClient") RestClient aiServiceStreamRestClient) {
    this.client = aiServiceStreamRestClient;
  }

  @Override
  public ResultatPlanification proposer(ContextePlanification contexte) {
    try {
      ResultatPlanification resultat =
          client
              .post()
              .uri("/internal/ai/v1/planification/proposer")
              .body(contexte.avecCorrelationId(CorrelationIdFilter.correlationIdCourant()))
              .retrieve()
              .body(ResultatPlanification.class);
      if (resultat == null) {
        throw new ServiceIndisponibleException("Réponse vide du service IA (planification)");
      }
      return resultat;
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le service IA (planification) est momentanément indisponible", e);
    }
  }
}

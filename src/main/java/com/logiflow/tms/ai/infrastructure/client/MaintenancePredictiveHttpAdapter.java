package com.logiflow.tms.ai.infrastructure.client;

import com.logiflow.tms.ai.domain.model.maintenance.ContexteMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.ai.domain.port.out.MaintenancePredictiveClientPort;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.infrastructure.web.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur HTTP vers l'agent de maintenance prédictive ({@code POST
 * /internal/ai/v1/maintenance/recommander}). Client au délai de lecture long : la synthèse est
 * rédigée par le LLM.
 */
@Component
public class MaintenancePredictiveHttpAdapter implements MaintenancePredictiveClientPort {

  private final RestClient client;

  public MaintenancePredictiveHttpAdapter(
      @Qualifier("aiServiceStreamRestClient") RestClient aiServiceStreamRestClient) {
    this.client = aiServiceStreamRestClient;
  }

  @Override
  public ResultatMaintenance recommander(ContexteMaintenance contexte) {
    try {
      ResultatMaintenance resultat =
          client
              .post()
              .uri("/internal/ai/v1/maintenance/recommander")
              .body(contexte.avecCorrelationId(CorrelationIdFilter.correlationIdCourant()))
              .retrieve()
              .body(ResultatMaintenance.class);
      if (resultat == null) {
        throw new ServiceIndisponibleException("Réponse vide du service IA (maintenance)");
      }
      return resultat;
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le service IA (maintenance) est momentanément indisponible", e);
    }
  }
}

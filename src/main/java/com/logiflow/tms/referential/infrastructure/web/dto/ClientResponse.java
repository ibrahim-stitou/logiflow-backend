package com.logiflow.tms.referential.infrastructure.web.dto;

import com.logiflow.tms.referential.domain.model.Client;
import java.util.UUID;

public record ClientResponse(UUID id, String code, String raisonSociale, boolean actif) {

  public static ClientResponse depuis(Client client) {
    return new ClientResponse(
        client.id(), client.code(), client.raisonSociale(), client.estActif());
  }
}

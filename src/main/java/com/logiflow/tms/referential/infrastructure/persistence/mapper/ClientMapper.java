package com.logiflow.tms.referential.infrastructure.persistence.mapper;

import com.logiflow.tms.referential.domain.model.Client;
import com.logiflow.tms.referential.infrastructure.persistence.entity.ClientEntity;
import java.util.UUID;
import org.mapstruct.Mapper;

/**
 * Traduit entre le modèle de domaine {@link Client} et l'entité JPA {@link ClientEntity}.
 *
 * <p>La conversion est portée par des méthodes {@code default} plutôt que par la génération
 * automatique MapStruct, car {@link Client} n'expose pas de constructeur public ni de mutateurs
 * JavaBean (invariants imposés via des fabriques statiques).
 */
@Mapper(componentModel = "spring")
public interface ClientMapper {

  // TODO vérifier que le tenant courant sera lu depuis le contexte de sécurité une fois le module
  // iam implémenté ; en attendant, un tenant par défaut est utilisé (application mono-tenant).
  UUID TENANT_PAR_DEFAUT = UUID.fromString("00000000-0000-0000-0000-000000000000");

  default Client versDomaine(ClientEntity entity) {
    if (entity == null) {
      return null;
    }
    return Client.reconstituer(
        entity.getId(), entity.getCode(), entity.getRaisonSociale(), entity.isActif());
  }

  default ClientEntity versEntite(Client client) {
    if (client == null) {
      return null;
    }
    return ClientEntity.builder()
        .id(client.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .code(client.code())
        .raisonSociale(client.raisonSociale())
        .actif(client.estActif())
        .build();
  }
}

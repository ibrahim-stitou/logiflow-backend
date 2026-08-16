package com.logiflow.tms.order.infrastructure.persistence.mapper;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.model.StatutCommande;
import com.logiflow.tms.order.infrastructure.persistence.entity.CommandeEntity;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.Currency;
import java.util.UUID;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Commande} et l'entité JPA {@link CommandeEntity}. */
@Mapper(componentModel = "spring")
public interface CommandeMapper {

  UUID TENANT_PAR_DEFAUT = UUID.fromString("00000000-0000-0000-0000-000000000000");

  default Commande versDomaine(CommandeEntity entity) {
    if (entity == null) {
      return null;
    }
    return Commande.reconstituer(
        entity.getId(),
        new Reference(entity.getReference()),
        entity.getClientId(),
        StatutCommande.valueOf(entity.getStatut()),
        entity.getDateSouhaitee(),
        new Money(entity.getPrixMontant(), Currency.getInstance(entity.getPrixDevise())));
  }

  default CommandeEntity versEntite(Commande commande) {
    if (commande == null) {
      return null;
    }
    return CommandeEntity.builder()
        .id(commande.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .reference(commande.reference().valeur())
        .clientId(commande.clientId())
        .statut(commande.statut().name())
        .dateSouhaitee(commande.dateSouhaitee())
        .prixMontant(commande.prixNegocie().montant())
        .prixDevise(commande.prixNegocie().devise().getCurrencyCode())
        .build();
  }
}

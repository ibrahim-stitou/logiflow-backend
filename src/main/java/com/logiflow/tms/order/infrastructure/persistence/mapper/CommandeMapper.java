package com.logiflow.tms.order.infrastructure.persistence.mapper;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.model.StatutCommande;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.order.infrastructure.persistence.entity.CommandeEntity;
import com.logiflow.tms.order.infrastructure.persistence.entity.LigneCommandeEntity;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Commande} et l'entité JPA {@link CommandeEntity}. */
@Mapper(componentModel = "spring")
public interface CommandeMapper {

  default Commande versDomaine(CommandeEntity entity, List<LigneCommandeEntity> lignes) {
    if (entity == null) {
      return null;
    }
    return Commande.reconstituer(
        entity.getId(),
        new Reference(entity.getReference()),
        entity.getClientId(),
        StatutCommande.valueOf(entity.getStatut()),
        entity.getDateSouhaitee(),
        new Money(entity.getPrixMontant(), Currency.getInstance(entity.getPrixDevise())),
        lignes.stream().map(this::versLigneDomaine).toList());
  }

  default CommandeEntity versEntite(Commande commande) {
    if (commande == null) {
      return null;
    }
    return CommandeEntity.builder()
        .id(commande.id())
        .reference(commande.reference().valeur())
        .clientId(commande.clientId())
        .statut(commande.statut().name())
        .dateSouhaitee(commande.dateSouhaitee())
        .prixMontant(commande.prixNegocie().montant())
        .prixDevise(commande.prixNegocie().devise().getCurrencyCode())
        .build();
  }

  default LigneCommande versLigneDomaine(LigneCommandeEntity entity) {
    return new LigneCommande(
        entity.getMarchandiseId(), entity.getPoidsKg(), entity.getVolumeM3(), entity.getNbColis());
  }

  default LigneCommandeEntity versLigneEntite(UUID commandeId, LigneCommande ligne) {
    return LigneCommandeEntity.builder()
        .commandeId(commandeId)
        .marchandiseId(ligne.marchandiseId())
        .poidsKg(ligne.poidsKg())
        .volumeM3(ligne.volumeM3())
        .nbColis(ligne.nbColis())
        .build();
  }
}

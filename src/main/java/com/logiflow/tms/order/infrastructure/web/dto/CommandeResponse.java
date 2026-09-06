package com.logiflow.tms.order.infrastructure.web.dto;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CommandeResponse(
    UUID id,
    String reference,
    UUID clientId,
    String statut,
    LocalDate dateSouhaitee,
    Money prixNegocie,
    List<LigneCommande> lignes) {

  public static CommandeResponse depuis(Commande commande) {
    return new CommandeResponse(
        commande.id(),
        commande.reference().valeur(),
        commande.clientId(),
        commande.statut().name(),
        commande.dateSouhaitee(),
        commande.prixNegocie(),
        commande.lignes());
  }
}

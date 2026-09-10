package com.logiflow.tms.order.application.command;

import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Commande applicative de création d'une commande client. */
public record CreerCommandeCommand(
    UUID clientId, LocalDate dateSouhaitee, Money prixNegocie, List<LigneCommande> lignes) {}

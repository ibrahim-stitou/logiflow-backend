package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.order.api.CommandeApi;
import com.logiflow.tms.referential.api.ClientApi;
import com.logiflow.tms.referential.api.dto.ClientSummary;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class RechercherCommandesOutil implements OutilCopilote {

  private final CommandeApi commandeApi;
  private final ClientApi clientApi;

  RechercherCommandesOutil(CommandeApi commandeApi, ClientApi clientApi) {
    this.commandeApi = commandeApi;
    this.clientApi = clientApi;
  }

  @Override
  public String nom() {
    return "rechercher_commandes";
  }

  @Override
  public String libelle() {
    return "Recherche des commandes clients";
  }

  @Override
  public String description() {
    return "Recherche les commandes clients par référence et/ou statut, de la plus récente à la "
        + "plus ancienne. Renvoie référence, statut, client et date souhaitée, ainsi que le "
        + "nombre total de commandes correspondantes.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("reference", "Référence complète ou partielle de la commande"),
        SchemaOutil.enumere("statut", "Statut de la commande", commandeApi.statutsConnus()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_COMMERCIAL;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        commandeApi.rechercher(
            arguments.texte("reference"),
            arguments.enumere("statut", commandeApi.statutsConnus()),
            arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream()
            .map(
                c ->
                    ligne(
                        "reference",
                        c.reference(),
                        "statut",
                        c.statut(),
                        "client",
                        c.clientId() == null
                            ? null
                            : clientApi
                                .consulter(c.clientId())
                                .map(ClientSummary::raisonSociale)
                                .orElse(null),
                        "dateSouhaitee",
                        c.dateSouhaitee() == null ? null : c.dateSouhaitee().toString()))
            .toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(c -> new SourceCopilote("COMMANDE", c.reference(), c.id().toString()))
            .toList());
  }
}

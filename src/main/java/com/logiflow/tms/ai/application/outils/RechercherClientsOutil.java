package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.referential.api.ClientApi;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class RechercherClientsOutil implements OutilCopilote {

  private final ClientApi clientApi;

  RechercherClientsOutil(ClientApi clientApi) {
    this.clientApi = clientApi;
  }

  @Override
  public String nom() {
    return "rechercher_clients";
  }

  @Override
  public String libelle() {
    return "Recherche des clients";
  }

  @Override
  public String description() {
    return "Recherche les clients par raison sociale ou code client. Renvoie code, raison sociale "
        + "et état actif, ainsi que le nombre total de clients correspondants.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("texte", "Raison sociale ou code client (partiel)"),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_COMMERCIAL;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page = clientApi.rechercher(arguments.texte("texte"), arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream()
            .map(
                c ->
                    ligne(
                        "code", c.code(),
                        "raisonSociale", c.raisonSociale(),
                        "actif", c.actif()))
            .toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(c -> new SourceCopilote("CLIENT", c.raisonSociale(), c.id().toString()))
            .toList());
  }
}

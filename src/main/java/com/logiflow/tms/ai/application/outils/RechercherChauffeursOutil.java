package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.driver.api.ChauffeurApi;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class RechercherChauffeursOutil implements OutilCopilote {

  private final ChauffeurApi chauffeurApi;

  RechercherChauffeursOutil(ChauffeurApi chauffeurApi) {
    this.chauffeurApi = chauffeurApi;
  }

  @Override
  public String nom() {
    return "rechercher_chauffeurs";
  }

  @Override
  public String libelle() {
    return "Recherche des chauffeurs";
  }

  @Override
  public String description() {
    return "Recherche les chauffeurs par nom, prénom ou matricule et/ou disponibilité "
        + "opérationnelle (ex. DISPONIBLE pour les chauffeurs libres). Renvoie matricule, nom, "
        + "statut administratif, disponibilité et solde de temps de conduite restant (minutes), "
        + "ainsi que le nombre total de chauffeurs correspondants.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("texte", "Nom, prénom ou matricule (partiel)"),
        SchemaOutil.enumere(
            "disponibilite", "Disponibilité opérationnelle", chauffeurApi.disponibilitesConnues()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        chauffeurApi.rechercher(
            arguments.texte("texte"),
            arguments.enumere("disponibilite", chauffeurApi.disponibilitesConnues()),
            arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream()
            .map(
                c ->
                    ligne(
                        "matricule", c.matricule(),
                        "nom", c.prenom() + " " + c.nom(),
                        "statut", c.statut(),
                        "disponibilite", c.disponibilite(),
                        "soldeTempsConduiteMinutes", c.soldeTempsConduiteMinutes()))
            .toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(
                c -> new SourceCopilote("CHAUFFEUR", c.prenom() + " " + c.nom(), c.id().toString()))
            .toList());
  }
}

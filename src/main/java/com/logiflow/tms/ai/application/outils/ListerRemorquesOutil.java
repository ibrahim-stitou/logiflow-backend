package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.RemorqueApi;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class ListerRemorquesOutil implements OutilCopilote {

  private final RemorqueApi remorqueApi;

  ListerRemorquesOutil(RemorqueApi remorqueApi) {
    this.remorqueApi = remorqueApi;
  }

  @Override
  public String nom() {
    return "lister_remorques";
  }

  @Override
  public String libelle() {
    return "Consultation des remorques";
  }

  @Override
  public String description() {
    return "Liste les remorques et semi-remorques filtrées par immatriculation et/ou statut. "
        + "Renvoie immatriculation, statut, volume utile (m3), positions palettes et charge utile "
        + "(kg), ainsi que le nombre total de remorques correspondantes.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("immatriculation", "Immatriculation complète ou partielle"),
        SchemaOutil.enumere("statut", "Statut de la remorque", remorqueApi.statutsConnus()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        remorqueApi.rechercher(
            arguments.texte("immatriculation"),
            arguments.enumere("statut", remorqueApi.statutsConnus()),
            arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream()
            .map(
                r ->
                    ligne(
                        "immatriculation", r.immatriculation(),
                        "statut", r.statut(),
                        "volumeUtileM3", r.volumeUtileM3(),
                        "positionsPalettes", r.nbPositionsPalettes(),
                        "chargeUtileKg", r.chargeUtileKg()))
            .toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(r -> new SourceCopilote("REMORQUE", r.immatriculation(), r.id().toString()))
            .toList());
  }
}

package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.dossier.api.DossierApi;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class RechercherDossiersOutil implements OutilCopilote {

  private final DossierApi dossierApi;

  RechercherDossiersOutil(DossierApi dossierApi) {
    this.dossierApi = dossierApi;
  }

  @Override
  public String nom() {
    return "rechercher_dossiers";
  }

  @Override
  public String libelle() {
    return "Recherche des dossiers de transport";
  }

  @Override
  public String description() {
    return "Recherche les dossiers de transport (expéditions) par référence (ex. DOS-2026-00012) "
        + "et/ou statut, du plus récent au plus ancien. Renvoie référence, statut, poids (kg), "
        + "volume (m3), palettes et présence de marchandises dangereuses (ADR), ainsi que le "
        + "nombre total de dossiers correspondants.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("reference", "Référence complète ou partielle du dossier"),
        SchemaOutil.enumere("statut", "Statut du dossier", dossierApi.statutsConnus()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_COMMERCIAL;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        dossierApi.rechercher(
            arguments.texte("reference"),
            arguments.enumere("statut", dossierApi.statutsConnus()),
            arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream()
            .map(
                d ->
                    ligne(
                        "reference", d.reference(),
                        "statut", d.statut(),
                        "poidsBrutKg", d.poidsBrutKg(),
                        "volumeM3", d.volumeM3(),
                        "nbPalettes", d.nbPalettes(),
                        "adr", d.contientAdr(),
                        "groupable", d.groupable()))
            .toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(d -> new SourceCopilote("DOSSIER", d.reference(), d.id().toString()))
            .toList());
  }
}

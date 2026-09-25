package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.SinistreSummary;
import com.logiflow.tms.shared.domain.DeviseApplication;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/** Outil copilote : sinistres de la flotte ou d'un engin, avec leur coût net. */
@Component
class RechercherSinistresOutil implements OutilCopilote {

  private static final List<String> STATUTS =
      List.of(
          "DECLARE",
          "DECLARE_ASSUREUR",
          "EN_EXPERTISE",
          "EN_REPARATION",
          "CLOS",
          "CLASSE_SANS_SUITE",
          "OUVERTS");
  private static final Set<String> CLOTURES = Set.of("CLOS", "CLASSE_SANS_SUITE");
  private static final ZoneId FUSEAU = ZoneId.of("Europe/Paris");

  private final MaintenanceApi maintenanceApi;
  private final ResolveurEngin resolveurEngin;

  RechercherSinistresOutil(MaintenanceApi maintenanceApi, ResolveurEngin resolveurEngin) {
    this.maintenanceApi = maintenanceApi;
    this.resolveurEngin = resolveurEngin;
  }

  @Override
  public String nom() {
    return "rechercher_sinistres";
  }

  @Override
  public String libelle() {
    return "Recherche de sinistres";
  }

  @Override
  public String description() {
    return "Recherche les sinistres (accidents, accrochages, vols, bris de glace…) d'un véhicule ou "
        + "d'une remorque (par immatriculation) ou de toute la flotte, sur une période (12 derniers "
        + "mois par défaut). Chaque ligne donne la date, la nature, la gravité, la responsabilité, "
        + "le statut du dossier assurance, l'immobilisation et le coût net pour l'entreprise "
        + "(réparations − indemnités). statut = OUVERTS pour les dossiers non clos.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte(
            "immatriculation", "Immatriculation de l'engin (absent = toute la flotte)"),
        SchemaOutil.texte("statut", "Statut du dossier : " + String.join(", ", STATUTS)),
        SchemaOutil.date("depuis", "Début de la période (AAAA-MM-JJ, défaut : il y a 12 mois)"),
        SchemaOutil.date("jusqua", "Fin de la période incluse (AAAA-MM-JJ, défaut : aujourd'hui)"),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    String immatriculation = arguments.texte("immatriculation");
    String statut = arguments.enumere("statut", STATUTS);
    LocalDate aujourdHui = LocalDate.now(FUSEAU);
    LocalDate jusqua = arguments.date("jusqua") == null ? aujourdHui : arguments.date("jusqua");
    LocalDate depuis =
        arguments.date("depuis") == null ? jusqua.minusMonths(12) : arguments.date("depuis");
    UUID enginId =
        immatriculation == null ? null : resolveurEngin.parImmatriculation(immatriculation).id();

    List<SinistreSummary> sinistres =
        maintenanceApi.sinistres(enginId, depuis, jusqua).stream()
            .filter(
                s ->
                    statut == null
                        || ("OUVERTS".equals(statut)
                            ? !CLOTURES.contains(s.statut())
                            : statut.equals(s.statut())))
            .sorted(Comparator.comparing(SinistreSummary::dateSurvenance).reversed())
            .toList();

    var libelles = resolveurEngin.libelles();
    List<Map<String, Object>> lignes = new ArrayList<>();
    List<SourceCopilote> sources = new ArrayList<>();
    for (SinistreSummary s : sinistres.stream().limit(arguments.limite()).toList()) {
      lignes.add(
          ligne(
              "reference", s.reference(),
              "date", s.dateSurvenance().toString(),
              "engins",
                  Stream.of(s.vehiculeId(), s.remorqueId())
                      .map(libelles::immatriculation)
                      .filter(Objects::nonNull)
                      .toList(),
              "nature", s.type(),
              "gravite", s.gravite(),
              "responsabilite", s.responsabilite(),
              "statut", s.statut(),
              "enginImmobilise", s.enginImmobilise(),
              "coutNet", s.coutNet(),
              "devise", DeviseApplication.PAR_DEFAUT.getCurrencyCode()));
      sources.add(new SourceCopilote("SINISTRE", s.reference(), s.id().toString()));
    }
    return new ResultatOutil(lignes, sinistres.size(), sources);
  }
}

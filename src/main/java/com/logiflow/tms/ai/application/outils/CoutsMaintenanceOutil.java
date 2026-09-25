package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.CoutsMaintenanceSummary;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import com.logiflow.tms.shared.domain.DeviseApplication;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Outil copilote : coûts de maintenance et sinistralité sur une période. */
@Component
class CoutsMaintenanceOutil implements OutilCopilote {

  private static final List<String> TYPES_ENGIN = List.of("VEHICULE", "REMORQUE");
  private static final ZoneId FUSEAU = ZoneId.of("Europe/Paris");

  private final MaintenanceApi maintenanceApi;

  CoutsMaintenanceOutil(MaintenanceApi maintenanceApi) {
    this.maintenanceApi = maintenanceApi;
  }

  @Override
  public String nom() {
    return "couts_maintenance";
  }

  @Override
  public String libelle() {
    return "Coûts de maintenance";
  }

  @Override
  public String description() {
    return "Coûts de maintenance sur une période (12 derniers mois par défaut) : total HT/TTC des "
        + "ordres de travail terminés, écart au budget estimé, répartition par type "
        + "d'intervention et par nature (préventif, correctif, réglementaire), engins les plus "
        + "coûteux, et sinistralité (nombre de sinistres, coût net, indemnités perçues). Filtre "
        + "facultatif : typeEngin = VEHICULE ou REMORQUE.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.date("depuis", "Début de la période (AAAA-MM-JJ, défaut : il y a 12 mois)"),
        SchemaOutil.date("jusqua", "Fin de la période incluse (AAAA-MM-JJ, défaut : aujourd'hui)"),
        SchemaOutil.texte("typeEngin", "VEHICULE ou REMORQUE (absent = toute la flotte)"));
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    LocalDate aujourdHui = LocalDate.now(FUSEAU);
    LocalDate jusqua = arguments.date("jusqua") == null ? aujourdHui : arguments.date("jusqua");
    LocalDate depuis =
        arguments.date("depuis") == null
            ? jusqua.minusMonths(12).plusDays(1)
            : arguments.date("depuis");
    if (depuis.isAfter(jusqua)) {
      throw new ValidationException(
          "La période est inversée.", List.of("depuis: postérieure à jusqua"));
    }
    CoutsMaintenanceSummary c =
        maintenanceApi.couts(depuis, jusqua, arguments.enumere("typeEngin", TYPES_ENGIN));

    List<Map<String, Object>> lignes =
        List.of(
            ligne(
                "periode", c.debut() + " → " + c.fin(),
                "devise", DeviseApplication.PAR_DEFAUT.getCurrencyCode(),
                "totalHt", c.totalHt(),
                "totalTtc", c.totalTtc(),
                "ordresTermines", c.nombreOrdres(),
                "budgetEstime", c.budgetEstime(),
                "ecartBudget",
                    c.budgetEstime().signum() == 0 ? null : c.totalHt().subtract(c.budgetEstime()),
                "sinistres", c.nombreSinistres(),
                "coutNetSinistres", c.coutNetSinistres(),
                "indemnitesPercues", c.indemnitesPercues()),
            ligne("repartition", "PAR_TYPE", "postes", postes(c.parType())),
            ligne("repartition", "PAR_NATURE", "postes", postes(c.parNature())),
            ligne("repartition", "ENGINS_LES_PLUS_COUTEUX", "postes", postes(c.parEngin())));
    return new ResultatOutil(
        lignes,
        lignes.size(),
        List.of(new SourceCopilote("COUTS_MAINTENANCE", "Coûts de maintenance", null)));
  }

  private static List<String> postes(List<CoutsMaintenanceSummary.Poste> postes) {
    return postes.stream()
        .map(
            p ->
                "%s : %s %s HT (%d OT)"
                    .formatted(
                        p.libelle(),
                        p.totalHt(),
                        DeviseApplication.PAR_DEFAUT.getCurrencyCode(),
                        p.nombre()))
        .toList();
  }
}

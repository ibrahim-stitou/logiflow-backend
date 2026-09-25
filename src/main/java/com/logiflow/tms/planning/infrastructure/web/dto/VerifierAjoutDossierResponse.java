package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.VoyageDossierService.ResultatVerificationAjout;
import com.logiflow.tms.shared.domain.exception.RemorqueCapaciteDepasseeException.TronconEnEchec;
import java.util.List;
import java.util.UUID;

/** Résultat du contrôle à blanc d'ajout d'un dossier à un voyage. */
public record VerifierAjoutDossierResponse(
    boolean compatible, DeviationResponse deviation, List<TronconEchecResponse> failedLegs) {

  public record DeviationResponse(
      String point, double detourKm, double detourPercent, double maxAllowedPercent) {}

  public record TronconEchecResponse(
      UUID arretDepartId,
      UUID arretArriveeId,
      String motif,
      Double depassementKg,
      Double depassementM3) {}

  public static VerifierAjoutDossierResponse depuis(ResultatVerificationAjout resultat) {
    if (resultat.compatible()) {
      return new VerifierAjoutDossierResponse(true, null, List.of());
    }
    DeviationResponse deviation =
        resultat.deviation() == null
            ? null
            : new DeviationResponse(
                resultat.deviation().point(),
                resultat.deviation().detourKm(),
                resultat.deviation().detourPercent(),
                resultat.deviation().maxAllowedPercent());
    List<TronconEchecResponse> echecs =
        resultat.tronconsEnEchec().stream().map(VerifierAjoutDossierResponse::versTroncon).toList();
    return new VerifierAjoutDossierResponse(false, deviation, echecs);
  }

  private static TronconEchecResponse versTroncon(TronconEnEchec troncon) {
    return new TronconEchecResponse(
        troncon.arretDepartId(),
        troncon.arretArriveeId(),
        troncon.motif(),
        troncon.depassementKg(),
        troncon.depassementM3());
  }
}

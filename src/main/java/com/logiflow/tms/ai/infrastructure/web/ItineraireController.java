package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.ItineraireGeometrieService;
import com.logiflow.tms.ai.application.ItineraireService;
import com.logiflow.tms.ai.application.command.CalculerItineraireCommand;
import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireGeometrieResponse;
import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireResponse;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Façade REST de l'agent itinéraire, seul point d'accès exposé au frontend Angular — le calcul est
 * relayé en interne au service IA (Flask, moteur OSRM) via {@link ItineraireService}. Voir
 * docs/integration-ia.md. Aucun repli déterministe : indisponible renvoie 503 (voir {@link
 * ItineraireService}).
 */
@RestController
@Tag(name = "itineraire-controller", description = "Calcul d'itinéraire et géométrie routière (OSRM)")
@RequiredArgsConstructor
public class ItineraireController {

  private final ItineraireService itineraireService;
  private final ItineraireGeometrieService itineraireGeometrieService;

  @Operation(
      summary = "Calculer un itinéraire",
      description =
          "Distance et durée de conduite via le service IA (Flask/OSRM). "
              + "Inclut la géométrie routière lorsque OSRM est disponible.")
  @ApiResponse(responseCode = "200", description = "Itinéraire calculé")
  @ApiResponse(responseCode = "503", description = "Service IA ou OSRM indisponible")
  @PostMapping("/api/v1/ia/itineraires/calcul")
  public ItineraireResponse calculer(@Valid @RequestBody ItineraireRequest request) {
    var points = versPoints(request);
    var itineraire = itineraireService.calculer(new CalculerItineraireCommand(points));
    return ItineraireResponse.depuis(itineraire);
  }

  @Operation(
      summary = "Géométrie routière d'un itinéraire",
      description =
          "Polyligne OSRM le long des routes empruntées, pour affichage cartographique. "
              + "Ne nécessite pas le service IA Flask.")
  @ApiResponse(responseCode = "200", description = "Géométrie routière")
  @ApiResponse(responseCode = "503", description = "Moteur OSRM indisponible")
  @PostMapping("/api/v1/ia/itineraires/geometrie")
  public ItineraireGeometrieResponse calculerGeometrie(
      @Valid @RequestBody ItineraireRequest request) {
    return ItineraireGeometrieResponse.depuis(
        itineraireGeometrieService.calculer(versPoints(request)));
  }

  private static List<PointItineraire> versPoints(ItineraireRequest request) {
    return request.points().stream()
        .map(p -> new PointItineraire(new GeoPoint(p.latitude(), p.longitude()), p.libelle()))
        .toList();
  }
}

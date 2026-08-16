package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.ItineraireService;
import com.logiflow.tms.ai.application.command.CalculerItineraireCommand;
import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireResponse;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import jakarta.validation.Valid;
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
@RequiredArgsConstructor
public class ItineraireController {

  private final ItineraireService itineraireService;

  @PostMapping("/api/v1/ia/itineraires/calcul")
  public ItineraireResponse calculer(@Valid @RequestBody ItineraireRequest request) {
    var points =
        request.points().stream()
            .map(p -> new PointItineraire(new GeoPoint(p.latitude(), p.longitude()), p.libelle()))
            .toList();
    var itineraire = itineraireService.calculer(new CalculerItineraireCommand(points));
    return ItineraireResponse.depuis(itineraire);
  }
}

package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.command.CalculerItineraireCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.ItineraireCalcule;
import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.port.out.RouteGeometryPort;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation applicatif de l'agent itinéraire. Comme pour le copilote, aucun repli
 * déterministe pertinent n'existe en cas d'indisponibilité : estimer une distance routière sans
 * moteur de routing produirait un chiffre trompeur plutôt qu'une simple absence de résultat.
 * L'échec est journalisé puis propagé (HTTP 503 côté contrôleur).
 */
@Service
@RequiredArgsConstructor
public class ItineraireService {

  private final AiServiceClientPort aiServiceClientPort;
  private final InteractionIaRepository interactionRepository;
  private final RouteGeometryPort routeGeometryPort;

  @Transactional
  public ItineraireCalcule calculer(CalculerItineraireCommand command) {
    String resume = "Itinéraire sur %d point(s)".formatted(command.points().size());
    Instant debut = Instant.now();
    try {
      ItineraireCalcule itineraire = aiServiceClientPort.calculerItineraire(command.points());
      List<GeoPoint> geometrie = resoudreGeometrieSansEchec(command.points());
      journaliser(true, debut, resume, null);
      return new ItineraireCalcule(
          itineraire.distanceKm(), itineraire.dureeMin(), itineraire.segments(), geometrie);
    } catch (ServiceIndisponibleException e) {
      journaliser(false, debut, resume, e.getMessage());
      throw e;
    }
  }

  private List<GeoPoint> resoudreGeometrieSansEchec(List<PointItineraire> points) {
    try {
      return routeGeometryPort.resoudreGeometrie(points);
    } catch (ServiceIndisponibleException e) {
      return List.of();
    }
  }

  private void journaliser(boolean succes, Instant debut, String resume, String erreur) {
    long dureeMs = Duration.between(debut, Instant.now()).toMillis();
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(),
            TypeInteractionIa.ITINERAIRE,
            null,
            succes,
            dureeMs,
            resume,
            erreur));
  }
}

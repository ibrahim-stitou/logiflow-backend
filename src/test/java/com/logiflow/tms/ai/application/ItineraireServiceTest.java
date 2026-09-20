package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.command.CalculerItineraireCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.ItineraireCalcule;
import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.port.out.RouteGeometryPort;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItineraireServiceTest {

  @Mock private AiServiceClientPort aiServiceClientPort;
  @Mock private InteractionIaRepository interactionRepository;
  @Mock private RouteGeometryPort routeGeometryPort;

  private ItineraireService itineraireService;

  private final List<PointItineraire> points =
      List.of(
          new PointItineraire(new GeoPoint(48.8566, 2.3522), "Paris"),
          new PointItineraire(new GeoPoint(45.7640, 4.8357), "Lyon"));

  @BeforeEach
  void setUp() {
    itineraireService =
        new ItineraireService(aiServiceClientPort, interactionRepository, routeGeometryPort);
  }

  @Test
  void calculerRenvoieLItineraireEtJournaliseLeSucces() {
    ItineraireCalcule itineraireCalcule = new ItineraireCalcule(465.3, 258.4, List.of());
    when(aiServiceClientPort.calculerItineraire(points)).thenReturn(itineraireCalcule);
    when(routeGeometryPort.resoudreGeometrie(points))
        .thenReturn(
            List.of(
                new GeoPoint(48.8566, 2.3522),
                new GeoPoint(45.7640, 4.8357)));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ItineraireCalcule resultat = itineraireService.calculer(new CalculerItineraireCommand(points));

    assertThat(resultat.distanceKm()).isEqualTo(465.3);
    assertThat(resultat.geometrie()).hasSize(2);
    verify(interactionRepository).sauvegarder(any(InteractionIa.class));
  }

  @Test
  void calculerPropageEtJournaliseLIndisponibiliteDuServiceIa() {
    when(aiServiceClientPort.calculerItineraire(points))
        .thenThrow(new ServiceIndisponibleException("indisponible"));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertThatThrownBy(() -> itineraireService.calculer(new CalculerItineraireCommand(points)))
        .isInstanceOf(ServiceIndisponibleException.class);
    verify(interactionRepository).sauvegarder(any(InteractionIa.class));
  }
}

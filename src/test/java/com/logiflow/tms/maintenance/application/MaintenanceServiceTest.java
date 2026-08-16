package com.logiflow.tms.maintenance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.maintenance.application.command.CreerOrdreTravailCommand;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.domain.port.out.ScoreSanteRepository;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

  @Mock private PlanEntretienRepository planEntretienRepository;
  @Mock private OrdreTravailRepository ordreTravailRepository;
  @Mock private ScoreSanteRepository scoreSanteRepository;
  @Mock private VehiculeApi vehiculeApi;

  private MaintenanceService maintenanceService;

  @BeforeEach
  void setUp() {
    maintenanceService =
        new MaintenanceService(
            planEntretienRepository, ordreTravailRepository, scoreSanteRepository, vehiculeApi);
  }

  @Test
  void creerOrdreTravailEchoueSiLeVehiculeEstIntrouvable() {
    UUID vehiculeId = UUID.randomUUID();
    when(vehiculeApi.consulter(vehiculeId)).thenReturn(Optional.empty());
    CreerOrdreTravailCommand command =
        new CreerOrdreTravailCommand(
            vehiculeId,
            TypeIntervention.ENTRETIEN_PREVENTIF,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(200), Currency.getInstance("EUR")));

    assertThatThrownBy(() -> maintenanceService.creerOrdreTravail(command))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void creerOrdreTravailSauvegardeUnNouvelOrdre() {
    UUID vehiculeId = UUID.randomUUID();
    when(vehiculeApi.consulter(vehiculeId))
        .thenReturn(
            Optional.of(
                new VehiculeSummary(
                    vehiculeId, "AB-123-CD", "PORTEUR", 19000, 9000, "DISPONIBLE")));
    when(ordreTravailRepository.sauvegarder(any(OrdreTravail.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    CreerOrdreTravailCommand command =
        new CreerOrdreTravailCommand(
            vehiculeId,
            TypeIntervention.ENTRETIEN_PREVENTIF,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(200), Currency.getInstance("EUR")));

    UUID id = maintenanceService.creerOrdreTravail(command);

    assertThat(id).isNotNull();
  }
}

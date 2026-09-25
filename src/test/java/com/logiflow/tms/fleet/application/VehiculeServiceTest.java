package com.logiflow.tms.fleet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.fleet.application.command.CreerVehiculeCommand;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.port.out.VehiculeRepository;
import com.logiflow.tms.fleet.domain.service.FleetDomainService;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

  @Mock private VehiculeRepository vehiculeRepository;
  @Mock private DocumentApi documentApi;

  private VehiculeService vehiculeService;

  @BeforeEach
  void setUp() {
    vehiculeService =
        new VehiculeService(vehiculeRepository, new FleetDomainService(), documentApi);
  }

  private static CreerVehiculeCommand commandeVehicule() {
    return new CreerVehiculeCommand(
        "AB-123-CD",
        TypeVehicule.PORTEUR,
        null,
        null,
        null,
        null,
        null,
        null,
        19000,
        null,
        9000,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null);
  }

  @Test
  void creerVehiculeEchoueSiLImmatriculationEstDejaUtilisee() {
    CreerVehiculeCommand command = commandeVehicule();
    when(vehiculeRepository.existeParImmatriculation("AB-123-CD")).thenReturn(true);

    assertThatThrownBy(() -> vehiculeService.creerVehicule(command))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void creerVehiculeSauvegardeUnNouveauVehicule() {
    CreerVehiculeCommand command = commandeVehicule();
    when(vehiculeRepository.existeParImmatriculation("AB-123-CD")).thenReturn(false);
    when(vehiculeRepository.sauvegarder(any(Vehicule.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = vehiculeService.creerVehicule(command);

    assertThat(id).isNotNull();
  }

  @Test
  void estDisponibleRenvoieFauxSiLeVehiculeEstIntrouvable() {
    UUID id = UUID.randomUUID();
    when(vehiculeRepository.parId(id)).thenReturn(Optional.empty());

    assertThat(vehiculeService.estDisponible(id)).isFalse();
  }
}
